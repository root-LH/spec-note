package io.github.root_LH.specnote.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * "오늘"을 계산할 때 쓰는 시계.
 * - 서버가 UTC로 돌아가도 한국 날짜 기준으로 접수 상태·D-day를 계산하도록 시간대를 고정
 * - 테스트에서는 고정된 Clock으로 바꿔 끼워 날짜를 마음대로 정할 수 있음
 */
@Configuration
public class TimeConfig {

    public static final ZoneId KST = ZoneId.of("Asia/Seoul");

    @Bean
    public Clock clock() {
        return Clock.system(KST);
    }
}
