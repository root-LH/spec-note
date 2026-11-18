package io.github.root_LH.specnote.notification.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.type.NumericBooleanConverter;

/**
 * 회원별 알림 설정. 행이 없으면 기본값(이메일 ON)으로 취급.
 *
 * Boolean ↔ NUMBER(1) 변환: Oracle 23은 BOOLEAN 타입이 있어서 Hibernate가 boolean 컬럼을 기대하는데,
 * 테이블은 NUMBER(1)로 만들었으므로 0/1 숫자로 저장하도록 컨버터를 지정.
 */
@Entity
@Table(name = "notification_setting")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class NotificationSetting {

    /** member_id를 그대로 PK로 사용 (회원 1명당 설정 1개) */
    @Id
    @Column(name = "member_id")
    private Long memberId;

    @Convert(converter = NumericBooleanConverter.class)
    @Column(name = "email_enabled", nullable = false)
    private Boolean emailEnabled;

    @Convert(converter = NumericBooleanConverter.class)
    @Column(name = "telegram_enabled", nullable = false)
    private Boolean telegramEnabled;

    @Column(name = "telegram_chat_id", length = 50)
    private String telegramChatId;

    private NotificationSetting(Long memberId) {
        this.memberId = memberId;
        this.emailEnabled = true;
        this.telegramEnabled = false;
    }

    public static NotificationSetting defaultOf(Long memberId) {
        return new NotificationSetting(memberId);
    }

    public void changeEmailEnabled(boolean enabled) {
        this.emailEnabled = enabled;
    }
}
