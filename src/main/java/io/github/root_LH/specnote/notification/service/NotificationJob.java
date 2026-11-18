package io.github.root_LH.specnote.notification.service;

import io.github.root_LH.specnote.exam.domain.ExamSchedule;
import io.github.root_LH.specnote.exam.repository.ExamScheduleRepository;
import io.github.root_LH.specnote.member.domain.Member;
import io.github.root_LH.specnote.member.repository.MemberRepository;
import io.github.root_LH.specnote.notification.domain.NotificationLog;
import io.github.root_LH.specnote.notification.domain.NotificationSetting;
import io.github.root_LH.specnote.notification.domain.NotificationStatus;
import io.github.root_LH.specnote.notification.domain.NotificationType;
import io.github.root_LH.specnote.notification.dto.NotificationRunResult;
import io.github.root_LH.specnote.notification.repository.NotificationLogRepository;
import io.github.root_LH.specnote.notification.repository.NotificationSettingRepository;
import io.github.root_LH.specnote.notification.sender.EmailSender;
import io.github.root_LH.specnote.notification.sender.NotificationMessage;
import io.github.root_LH.specnote.subscription.domain.Subscription;
import io.github.root_LH.specnote.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 알림 배치의 핵심 로직.
 *
 * 흐름: 알림 종류별 기준 날짜 계산 → 해당 날짜의 일정 조회 → 구독자 조회
 *      → 발송 기록(PENDING) 생성 → 발송 → SENT / FAILED 갱신
 *
 * 이 클래스에는 @Transactional을 걸지 않음.
 * 메일 발송처럼 느린 외부 호출 동안 DB 트랜잭션(커넥션)을 붙잡고 있지 않기 위함.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationJob {

    static final int MAX_RETRY = 3;

    private final ExamScheduleRepository scheduleRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final NotificationSettingRepository settingRepository;
    private final NotificationLogRepository logRepository;
    private final MemberRepository memberRepository;
    private final NotificationLogService logService;
    private final NotificationMessageFactory messageFactory;
    private final EmailSender emailSender;
    private final Clock clock;

    /**
     * @param today 기준일. 스케줄러는 오늘 날짜로, 관리자 API는 원하는 날짜로 실행 가능
     */
    public NotificationRunResult run(LocalDate today) {
        Counter counter = new Counter();

        for (NotificationType type : NotificationType.scheduleTypes()) {
            LocalDate targetDate = type.targetDate(today);

            for (ExamSchedule schedule : findSchedules(type, targetDate)) {
                List<Subscription> subscriptions =
                        subscriptionRepository.findAllWithMemberByExamId(schedule.getExam().getId());
                if (subscriptions.isEmpty()) {
                    continue;
                }

                Map<Long, NotificationSetting> settings = loadSettings(subscriptions);
                NotificationMessage message = messageFactory.forSchedule(type, schedule);

                for (Subscription subscription : subscriptions) {
                    Member member = subscription.getMember();
                    if (!isEmailEnabled(settings.get(member.getId()))) {
                        counter.skipped++;
                        continue;
                    }

                    Optional<Long> logId = createLogIfAbsent(member.getId(), type, schedule.getId());
                    if (logId.isEmpty()) {
                        counter.skipped++;   // 이미 보낸 알림
                        continue;
                    }
                    deliver(logId.get(), member.getEmail(), message, counter);
                }
            }
        }

        NotificationRunResult result = counter.toResult();
        log.info("[알림 배치] 기준일={} 결과={}", today, result);
        return result;
    }

    /**
     * 실패한 알림 재발송 (최대 MAX_RETRY회)
     */
    public NotificationRunResult retryFailed() {
        Counter counter = new Counter();
        List<NotificationLog> failedLogs =
                logRepository.findTop100ByStatusAndRetryCountLessThanOrderByIdAsc(NotificationStatus.FAILED, MAX_RETRY);

        for (NotificationLog failed : failedLogs) {
            if (!failed.getType().isScheduleType()) {
                continue;   // 스펙 만료 알림은 4단계에서 추가
            }
            Optional<ExamSchedule> schedule = scheduleRepository.findWithExamById(failed.getTargetId());
            Optional<Member> member = memberRepository.findById(failed.getMemberId());
            if (schedule.isEmpty() || member.isEmpty()) {
                logService.markFailed(failed.getId());   // 대상이 삭제됨 → 횟수만 올려 언젠가 재시도 대상에서 빠지게 함
                counter.failed++;
                continue;
            }
            NotificationMessage message = messageFactory.forSchedule(failed.getType(), schedule.get());
            deliver(failed.getId(), member.get().getEmail(), message, counter);
        }

        if (!failedLogs.isEmpty()) {
            log.info("[알림 재발송] 결과={}", counter.toResult());
        }
        return counter.toResult();
    }

    private List<ExamSchedule> findSchedules(NotificationType type, LocalDate date) {
        return switch (type.getDateField()) {
            case REG_START -> scheduleRepository.findWithExamByRegStart(date);
            case REG_END -> scheduleRepository.findWithExamByRegEnd(date);
            case EXAM_DATE -> scheduleRepository.findWithExamByExamDate(date);
            case RESULT_DATE -> scheduleRepository.findWithExamByResultDate(date);
        };
    }

    /** 구독자들의 알림 설정을 한 번에 조회 (회원마다 따로 조회하면 N+1) */
    private Map<Long, NotificationSetting> loadSettings(List<Subscription> subscriptions) {
        List<Long> memberIds = subscriptions.stream()
                .map(subscription -> subscription.getMember().getId())
                .toList();
        return settingRepository.findAllById(memberIds).stream()
                .collect(Collectors.toMap(NotificationSetting::getMemberId, Function.identity()));
    }

    /** 설정 행이 없으면 기본값(이메일 ON) */
    private boolean isEmailEnabled(NotificationSetting setting) {
        return setting == null || Boolean.TRUE.equals(setting.getEmailEnabled());
    }

    /**
     * 1차로 exists 조회, 2차로 DB UNIQUE 제약이 중복을 막음.
     * (동시에 두 서버가 실행해도 한 곳만 기록에 성공 → 한 번만 발송)
     */
    private Optional<Long> createLogIfAbsent(Long memberId, NotificationType type, Long targetId) {
        if (logRepository.existsByMemberIdAndTypeAndTargetId(memberId, type, targetId)) {
            return Optional.empty();
        }
        try {
            return Optional.of(logService.createPending(memberId, type, targetId));
        } catch (DataIntegrityViolationException e) {
            return Optional.empty();
        }
    }

    private void deliver(Long logId, String to, NotificationMessage message, Counter counter) {
        try {
            emailSender.send(to, message);
            logService.markSent(logId, LocalDateTime.now(clock));
            counter.sent++;
        } catch (Exception e) {
            log.warn("[알림 발송 실패] logId={} to={} reason={}", logId, to, e.getMessage());
            logService.markFailed(logId);
            counter.failed++;
        }
    }

    private static class Counter {
        int sent;
        int failed;
        int skipped;

        NotificationRunResult toResult() {
            return new NotificationRunResult(sent, failed, skipped);
        }
    }
}
