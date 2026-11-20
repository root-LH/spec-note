package io.github.root_LH.specnote.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * 공공데이터포털(data.go.kr) API 설정.
 *
 * 인증키는 포털 계정당 하나이고, 활용신청한 모든 API에 같은 키를 씀
 * → serviceKey는 한 곳에만 두고, API별로는 주소와 옵션만 따로 관리.
 *
 * <pre>
 * public-data:
 *   service-key: (application-local.yml 또는 환경변수 PUBLIC_DATA_SERVICE_KEY)
 *   qnet:
 *     schedule-url: ...
 *     exam-list-url: ...
 *     series-names: [기사]
 * </pre>
 *
 * 나중에 다른 공공데이터 API를 쓰면 qnet처럼 하위 항목만 추가하면 됨.
 */
@ConfigurationProperties(prefix = "public-data")
public record PublicDataProperties(String serviceKey, Qnet qnet) {

    public PublicDataProperties {
        if (qnet == null) {
            qnet = new Qnet(null, null, null);
        }
    }

    /**
     * 한국산업인력공단(Q-Net) API
     *
     * @param scheduleUrl  국가자격 시험일정 조회 서비스 요청주소
     * @param examListUrl  국가자격 종목 목록 정보 요청주소
     * @param seriesNames  자동 등록할 계열(등급) 이름. 예: [기사], [기사, 산업기사]
     */
    public record Qnet(String scheduleUrl, String examListUrl, List<String> seriesNames) {

        public Qnet {
            if (!StringUtils.hasText(scheduleUrl)) {
                scheduleUrl = "https://apis.data.go.kr/B490007/qualExamSchd/getQualExamSchdList";
            }
            if (!StringUtils.hasText(examListUrl)) {
                examListUrl = "http://openapi.q-net.or.kr/api/service/rest/InquiryListNationalQualifcationSVC/getList";
            }
            if (seriesNames == null || seriesNames.isEmpty()) {
                seriesNames = List.of("기사");
            }
        }
    }

    public boolean hasServiceKey() {
        return StringUtils.hasText(serviceKey);
    }

    /**
     * URL에 붙일 인증키.
     * 포털은 Encoding 키와 Decoding 키를 둘 다 주는데, 어느 쪽을 넣어도 동작하도록
     * 이미 인코딩된 키(%가 들어 있음)는 그대로, 아니면 한 번만 인코딩.
     * (+ 문자가 인코딩되지 않으면 서버가 공백으로 해석해 "등록되지 않은 키" 오류가 남)
     */
    public String encodedServiceKey() {
        String key = serviceKey.trim();
        return key.contains("%") ? key : URLEncoder.encode(key, StandardCharsets.UTF_8);
    }
}
