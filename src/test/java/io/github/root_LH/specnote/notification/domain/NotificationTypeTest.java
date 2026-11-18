package io.github.root_LH.specnote.notification.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class NotificationTypeTest {

    private final LocalDate today = LocalDate.of(2026, 11, 28);

    @Test
    void 접수_시작_3일_전_알림은_3일_뒤_일정이_대상() {
        assertThat(NotificationType.REG_START_D3.targetDate(today)).isEqualTo(LocalDate.of(2026, 12, 1));
    }

    @Test
    void 발표일_알림은_오늘_일정이_대상() {
        assertThat(NotificationType.RESULT_D0.targetDate(today)).isEqualTo(today);
    }

    @Test
    void 월이_바뀌는_경우도_계산된다() {
        assertThat(NotificationType.EXPIRE_D30.targetDate(LocalDate.of(2026, 12, 15)))
                .isEqualTo(LocalDate.of(2027, 1, 14));
    }

    @Test
    void 일정_알림_종류는_4개() {
        assertThat(NotificationType.scheduleTypes())
                .containsExactly(
                        NotificationType.REG_START_D3,
                        NotificationType.REG_END_D1,
                        NotificationType.EXAM_D1,
                        NotificationType.RESULT_D0);
    }
}
