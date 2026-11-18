package io.github.root_LH.specnote.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Scheduled 사용을 켬 (매일 알림 발송, 실패 재발송)
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
