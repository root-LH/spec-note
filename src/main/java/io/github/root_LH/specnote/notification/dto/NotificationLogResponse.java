package io.github.root_LH.specnote.notification.dto;

import io.github.root_LH.specnote.notification.domain.NotificationLog;
import io.github.root_LH.specnote.notification.domain.NotificationStatus;
import io.github.root_LH.specnote.notification.domain.NotificationType;

import java.time.LocalDateTime;

public record NotificationLogResponse(
        Long logId,
        NotificationType type,
        String typeName,
        Long targetId,
        NotificationStatus status,
        LocalDateTime sentAt,
        LocalDateTime createdAt
) {

    public static NotificationLogResponse from(NotificationLog log) {
        return new NotificationLogResponse(
                log.getId(),
                log.getType(),
                log.getType().getDescription(),
                log.getTargetId(),
                log.getStatus(),
                log.getSentAt(),
                log.getCreatedAt());
    }
}
