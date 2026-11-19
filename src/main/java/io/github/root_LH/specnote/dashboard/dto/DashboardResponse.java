package io.github.root_LH.specnote.dashboard.dto;

import io.github.root_LH.specnote.exam.dto.ScheduleResponse;
import io.github.root_LH.specnote.spec.dto.SpecResponse;

import java.util.List;

/**
 * 대시보드 한 화면에 필요한 데이터를 한 번에 응답
 */
public record DashboardResponse(
        String nickname,
        SpecSummary specSummary,
        List<SpecResponse> expiringSpecs,          // 만료 예정 (가까운 순)
        int subscriptionCount,
        List<ScheduleResponse> upcomingSchedules   // 구독한 시험의 다가오는 일정
) {

    public record SpecSummary(
            int total,
            int permanent,
            int active,
            int expiringSoon,
            int expired
    ) {
    }
}
