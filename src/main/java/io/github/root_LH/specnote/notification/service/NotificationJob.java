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
import io.github.root_LH.specnote.spec.domain.MemberSpec;
import io.github.root_LH.specnote.spec.repository.MemberSpecRepository;
import io.github.root_LH.specnote.subscription.domain.Subscription;
import io.github.root_LH.specnote.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 알림 배치의 핵심 로직.
 *
 * 1) 시험 일정 알림: 알림 종류별 기준 날짜의 일정 → 구독자에게 발송
 * 2) 스펙 만료 알림: 만료일이 기준 날짜인 스펙 → 보유자에게 발송
 *
 * 공통 흐름: 발송 기록(PENDING) 생성 → 발송 → SENT / FAILED 갱신
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
    private final MemberSpecRepository specRepository;
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
        sendScheduleNotifications(today, counter);
        sendSpecExpireNotifications(today, counter);

        NotificationRunResult result = counter.toResult();
        log.info("[알림 배치] 기준일={} 결과={}", today, result);
        return result;
    }

    private void sendScheduleNotifications(LocalDate today, Counter counter) {
        for (NotificationType type : NotificationType.scheduleTypes()) {
            for (ExamSchedule schedule : findSchedules(type, type.targetDate(today))) {
                List<Subscription> subscriptions =
                        subscriptionRepository.findAllWithMemberByExamId(schedule.getExam().getId());
                if (subscriptions.isEmpty()) {
                    continue;
                }

                List<Member> members = subscriptions.stream().map(Subscription::getMember).toList();
                Map<Long, NotificationSetting> settings = loadSettings(members);
                NotificationMessage message = messageFactory.forSchedule(type, schedule);

                for (Member member : members) {
                    notifyOnce(member, settings.get(member.getId()), type, schedule.getId(), message, counter);
                }
            }
        }
    }

    private void sendSpecExpireNotifications(LocalDate today, Counter counter) {
        for (NotificationType type : NotificationType.specTypes()) {
            List<MemberSpec> specs = specRepository.findWithMemberAndExamByExpireDate(type.targetDate(today));
            if (specs.isEmpty()) {
                continue;
            }

            Map<Long, NotificationSetting> settings = loadSettings(specs.stream().map(MemberSpec::getMember).toList());
            for (MemberSpec spec : specs) {
                Member member = spec.getMember();
                NotificationMessage message = messageFactory.forSpec(type, spec);
                notifyOnce(member, settings.get(member.getId()), type, spec.getId(), message, counter);
            }
        }
    }

    /** 알림 설정 확인 → 중복 확인 → 발송 */
    private void notifyOnce(Member member, NotificationSetting setting, NotificationType type,
                            Long targetId, NotificationMessage message, Counter counter) {
        if (!isEmailEnabled(setting)) {
            counter.skipped++;
            return;
        }
        Optional<Long> logId = createLogIfAbsent(member.getId(), type, targetId);
        if (logId.isEmpty()) {
            counter.skipped++;   // 이미 보낸 알림
            return;
        }
        deliver(logId.get(), member.getEmail(), message, counter);
    }

    /**
     * 실패한 알림 재발송 (최대 MAX_RETRY회)
     */
    public NotificationRunResult retryFailed() {
        Counter counter = new Counter();
        List<NotificationLog> failedLogs =
                logRepository.findTop100ByStatusAndRetryCountLessThanOrderByIdAsc(NotificationStatus.FAILED, MAX_RETRY);

        for (NotificationLog failed : failedLogs) {
            Optional<Member> member = memberRepository.findById(failed.getMemberId());
            Optional<NotificationMessage> message = rebuildMessage(failed);

            if (member.isEmpty() || message.isEmpty()) {
                // 대상이 삭제됨 → 횟수만 올려서 결국 재시도 대상에서 빠지게 함
                logService.markFailed(failed.getId());
                counter.failed++;
                continue;
            }
            deliver(failed.getId(), member.get().getEmail(), message.get(), counter);
        }

        if (!failedLogs.isEmpty()) {
            log.info("[알림 재발송] 결과={}", counter.toResult());
        }
        return counter.toResult();
    }

    private Optional<NotificationMessage> rebuildMessage(NotificationLog failed) {
        NotificationType type = failed.getType();
        if (type.isScheduleType()) {
            return scheduleRepository.findWithExamById(failed.getTargetId())
                    .map(schedule -> messageFactory.forSchedule(type, schedule));
        }
        return specRepository.findWithMemberAndExamById(failed.getTargetId())
                .map(spec -> messageFactory.forSpec(type, spec));
    }

    private List<ExamSchedule> findSchedules(NotificationType type, LocalDate date) {
        return switch (type.getDateField()) {
            case REG_START -> scheduleRepository.findWithExamByRegStart(date);
            case REG_END -> scheduleRepository.findWithExamByRegEnd(date);
            case EXAM_DATE -> scheduleRepository.findWithExamByExamDate(date);
            case RESULT_DATE -> scheduleRepository.findWithExamByResultDate(date);
        };
    }

    /** 대상 회원들의 알림 설정을 한 번에 조회 (회원마다 따로 조회하면 N+1) */
    private Map<Long, NotificationSetting> loadSettings(Collection<Member> members) {
        List<Long> memberIds = members.stream().map(Member::getId).distinct().toList();
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
