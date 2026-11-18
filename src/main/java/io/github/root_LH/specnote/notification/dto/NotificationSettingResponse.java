package io.github.root_LH.specnote.notification.dto;

import io.github.root_LH.specnote.notification.domain.NotificationSetting;

public record NotificationSettingResponse(
        boolean emailEnabled,
        boolean telegramEnabled,
        boolean telegramLinked
) {

    public static NotificationSettingResponse from(NotificationSetting setting) {
        return new NotificationSettingResponse(
                Boolean.TRUE.equals(setting.getEmailEnabled()),
                Boolean.TRUE.equals(setting.getTelegramEnabled()),
                setting.getTelegramChatId() != null);
    }
}
