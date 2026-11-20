package io.github.root_LH.specnote.exam.sync;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * 매일 새벽 3시에 시험 일정 동기화.
 * 알림 배치(오전 8시)보다 먼저 실행해서, 바뀐 일정으로 알림이 나가도록 함.
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnProperty(name = "notification.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class ExamSyncScheduler {

    private final ExamSyncService examSyncService;

    @Scheduled(cron = "0 0 3 * * *", zone = "Asia/Seoul")
    public void syncDaily() {
        if (!examSyncService.isAvailable()) {
            log.info("[시험 동기화] 인증키가 설정되지 않아 건너뜀");
            return;
        }
        examSyncService.syncAll();
    }
}
