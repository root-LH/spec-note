package io.github.root_LH.specnote.exam.external;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

/**
 * 공공데이터포털 - 한국산업인력공단 국가자격 시험일정 조회 서비스 설정
 *
 * @param baseUrl    요청 주소 (공공데이터포털 활용신청 상세의 "요청주소")
 * @param serviceKey 인증키. application-local.yml 또는 환경변수 QNET_SERVICE_KEY로 주입 (Git에 올리지 않음)
 */
@ConfigurationProperties(prefix = "public-data.qnet")
public record QnetProperties(String baseUrl, String serviceKey) {

    private static final String DEFAULT_BASE_URL =
            "https://apis.data.go.kr/B490007/qualExamSchd/getQualExamSchdList";

    public QnetProperties {
        if (!StringUtils.hasText(baseUrl)) {
            baseUrl = DEFAULT_BASE_URL;
        }
    }
}
