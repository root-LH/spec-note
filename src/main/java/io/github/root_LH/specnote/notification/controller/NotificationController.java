package io.github.root_LH.specnote.notification.controller;

import io.github.root_LH.specnote.notification.dto.NotificationLogResponse;
import io.github.root_LH.specnote.notification.dto.NotificationSettingResponse;
import io.github.root_LH.specnote.notification.dto.NotificationSettingUpdateRequest;
import io.github.root_LH.specnote.notification.service.NotificationSettingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Notification", description = "내 알림 설정·내역")
@RestController
@RequestMapping("/api/me")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationSettingService settingService;

    @Operation(summary = "알림 설정 조회")
    @GetMapping("/notification-settings")
    public NotificationSettingResponse getSetting(@Parameter(hidden = true) @AuthenticationPrincipal Long memberId) {
        return settingService.getSetting(memberId);
    }

    @Operation(summary = "알림 설정 변경")
    @PutMapping("/notification-settings")
    public NotificationSettingResponse updateSetting(@Parameter(hidden = true) @AuthenticationPrincipal Long memberId,
                                                     @Valid @RequestBody NotificationSettingUpdateRequest request) {
        return settingService.updateSetting(memberId, request);
    }

    @Operation(summary = "내 알림 내역", description = "최근 30건")
    @GetMapping("/notifications")
    public List<NotificationLogResponse> getMyNotifications(
            @Parameter(hidden = true) @AuthenticationPrincipal Long memberId) {
        return settingService.getMyNotifications(memberId);
    }
}
