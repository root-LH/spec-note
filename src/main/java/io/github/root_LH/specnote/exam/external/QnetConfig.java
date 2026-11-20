package io.github.root_LH.specnote.exam.external;

import io.github.root_LH.specnote.global.config.PublicDataProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(PublicDataProperties.class)
public class QnetConfig {

    /**
     * 외부 API 전용 HTTP 클라이언트.
     * 타임아웃을 꼭 지정: 외부 서버가 응답하지 않으면 배치 스레드가 무한정 멈출 수 있음
     */
    @Bean
    public RestClient qnetRestClient() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(5));
        requestFactory.setReadTimeout(Duration.ofSeconds(15));

        return RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }
}
