package io.github.root_LH.specnote.notification.domain;

public enum NotificationStatus {
    PENDING,   // 기록만 됨 (발송 전)
    SENT,      // 발송 성공
    FAILED     // 발송 실패 → 재시도 대상
}
