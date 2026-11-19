package io.github.root_LH.specnote.dashboard.controller;

import io.github.root_LH.specnote.dashboard.dto.DashboardResponse;
import io.github.root_LH.specnote.dashboard.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Dashboard", description = "내 스펙·일정 한눈에 보기")
@RestController
@RequestMapping("/api/me/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final DashboardService dashboardService;

    @Operation(summary = "대시보드", description = "스펙 현황, 만료 예정 스펙(90일 이내), 구독 시험의 다가오는 일정(최대 10개)")
    @GetMapping
    public DashboardResponse getDashboard(@Parameter(hidden = true) @AuthenticationPrincipal Long memberId) {
        return dashboardService.getDashboard(memberId);
    }
}
