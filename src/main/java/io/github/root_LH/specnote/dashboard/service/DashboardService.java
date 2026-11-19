package io.github.root_LH.specnote.dashboard.service;

import io.github.root_LH.specnote.dashboard.dto.DashboardResponse;
import io.github.root_LH.specnote.dashboard.dto.DashboardResponse.SpecSummary;
import io.github.root_LH.specnote.exam.dto.ScheduleResponse;
import io.github.root_LH.specnote.exam.repository.ExamScheduleRepository;
import io.github.root_LH.specnote.global.exception.BusinessException;
import io.github.root_LH.specnote.global.exception.ErrorCode;
import io.github.root_LH.specnote.member.domain.Member;
import io.github.root_LH.specnote.member.repository.MemberRepository;
import io.github.root_LH.specnote.spec.domain.SpecStatus;
import io.github.root_LH.specnote.spec.dto.SpecResponse;
import io.github.root_LH.specnote.spec.repository.MemberSpecRepository;
import io.github.root_LH.specnote.subscription.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DashboardService {

    /** 이 일수 안에 만료되는 스펙을 "만료 예정" 목록에 보여줌 */
    private static final int EXPIRING_LIST_DAYS = 90;
    private static final int UPCOMING_SCHEDULE_LIMIT = 10;

    private final MemberRepository memberRepository;
    private final MemberSpecRepository specRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final ExamScheduleRepository scheduleRepository;
    private final Clock clock;

    public DashboardResponse getDashboard(Long memberId) {
        LocalDate today = LocalDate.now(clock);
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new BusinessException(ErrorCode.MEMBER_NOT_FOUND));

        // 한 사람의 스펙은 많아야 수십 개라 한 번에 가져와 메모리에서 집계
        List<SpecResponse> specs = specRepository.findAllWithExamByMemberId(memberId).stream()
                .map(spec -> SpecResponse.of(spec, today))
                .toList();

        List<SpecResponse> expiringSpecs = specs.stream()
                .filter(spec -> spec.daysUntilExpire() != null)
                .filter(spec -> spec.daysUntilExpire() >= 0 && spec.daysUntilExpire() <= EXPIRING_LIST_DAYS)
                .sorted(Comparator.comparing(SpecResponse::daysUntilExpire))
                .toList();

        List<ScheduleResponse> upcomingSchedules = scheduleRepository
                .findUpcomingForSubscriber(memberId, today, PageRequest.of(0, UPCOMING_SCHEDULE_LIMIT)).stream()
                .map(schedule -> ScheduleResponse.of(schedule, today))
                .toList();

        int subscriptionCount = subscriptionRepository.findAllWithExamByMemberId(memberId).size();

        return new DashboardResponse(
                member.getNickname(),
                summarize(specs),
                expiringSpecs,
                subscriptionCount,
                upcomingSchedules);
    }

    private SpecSummary summarize(List<SpecResponse> specs) {
        return new SpecSummary(
                specs.size(),
                count(specs, SpecStatus.PERMANENT),
                count(specs, SpecStatus.ACTIVE),
                count(specs, SpecStatus.EXPIRING_SOON),
                count(specs, SpecStatus.EXPIRED));
    }

    private int count(List<SpecResponse> specs, SpecStatus status) {
        return (int) specs.stream().filter(spec -> spec.status() == status).count();
    }
}
