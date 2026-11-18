package io.github.root_LH.specnote.notification.dto;

import jakarta.validation.constraints.NotNull;

/**
 * 텔레그램 설정은 연동 기능을 만들 때 추가 예정
 */
public record NotificationSettingUpdateRequest(
        @NotNull
        Boolean emailEnabled
) {
}
