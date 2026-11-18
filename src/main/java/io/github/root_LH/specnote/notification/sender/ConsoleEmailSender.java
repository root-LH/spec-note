package io.github.root_LH.specnote.notification.sender;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 메일 설정이 없을 때 사용: 실제로 보내지 않고 콘솔에 내용을 출력
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "notification.email.enabled", havingValue = "false", matchIfMissing = true)
public class ConsoleEmailSender implements EmailSender {

    @Override
    public void send(String to, NotificationMessage message) {
        log.info("""

                ===== [메일 발송 대신 콘솔 출력] =====
                받는 사람: {}
                제목: {}
                {}
                ====================================""", to, message.title(), message.body());
    }
}
