package io.github.root_LH.specnote.notification.sender;

/**
 * 메일 발송 인터페이스.
 * 설정(notification.email.enabled)에 따라 구현체가 바뀜
 * - false(기본): ConsoleEmailSender → 콘솔 로그로 출력 (개발·테스트용)
 * - true: SmtpEmailSender → 실제 메일 발송
 *
 * 호출하는 쪽은 인터페이스만 알기 때문에 구현이 바뀌어도 코드를 고칠 필요가 없음.
 * 나중에 텔레그램 발송도 같은 방식으로 추가 가능.
 */
public interface EmailSender {

    /** 실패하면 예외를 던짐 → 호출하는 쪽에서 FAILED 처리 */
    void send(String to, NotificationMessage message);
}
