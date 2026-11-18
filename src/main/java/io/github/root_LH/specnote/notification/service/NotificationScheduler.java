package io.github.root_LH.specnote.notification.service;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDate;

/**
 * 정해진 시각에 알림 배치를 실행.
 * notification.scheduler.enabled: false 로 끌 수 있음 (테스트 등)
 */
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notification.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class NotificationScheduler {

    private final NotificationJob notificationJob;
    private final Clock clock;

    /** 매일 오전 8시 (한국 시간) */
    @Scheduled(cron = "0 0 8 * * *", zone = "Asia/Seoul")
    public void sendDailyNotifications() {
        notificationJob.run(LocalDate.now(clock));
    }

    /** 매시 30분: 실패 건 재발송 */
    @Scheduled(cron = "0 30 * * * *", zone = "Asia/Seoul")
    public void retryFailedNotifications() {
        notificationJob.retryFailed();
    }
}
