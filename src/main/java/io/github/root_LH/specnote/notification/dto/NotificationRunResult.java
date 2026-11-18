package io.github.root_LH.specnote.notification.dto;

/**
 * 배치 실행 결과
 * - sent: 발송 성공
 * - failed: 발송 실패 (재시도 예정)
 * - skipped: 이미 보냈거나 알림을 끈 회원
 */
public record NotificationRunResult(int sent, int failed, int skipped) {
}
