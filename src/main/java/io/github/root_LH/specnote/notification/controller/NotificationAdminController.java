package io.github.root_LH.specnote.notification.controller;

import io.github.root_LH.specnote.notification.dto.NotificationRunResult;
import io.github.root_LH.specnote.notification.service.NotificationJob;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Clock;
import java.time.LocalDate;

/**
 * 배치를 수동으로 실행 (테스트, 장애 후 재실행용).
 * 같은 날짜로 여러 번 실행해도 알림은 한 번만 나감.
 */
@Tag(name = "Admin - Notification", description = "관리자 전용 알림 배치 수동 실행")
@RestController
@RequestMapping("/api/admin/notifications")
@RequiredArgsConstructor
public class NotificationAdminController {

    private final NotificationJob notificationJob;
    private final Clock clock;

    @Operation(summary = "알림 배치 실행", description = "date를 비우면 오늘 기준. 예: 2026-11-28")
    @PostMapping("/run")
    public NotificationRunResult run(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return notificationJob.run(date != null ? date : LocalDate.now(clock));
    }

    @Operation(summary = "실패 알림 재발송")
    @PostMapping("/retry")
    public NotificationRunResult retry() {
        return notificationJob.retryFailed();
    }
}
