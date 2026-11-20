package io.github.root_LH.specnote.exam.external;

import io.github.root_LH.specnote.global.config.PublicDataProperties;
import io.github.root_LH.specnote.global.exception.BusinessException;
import io.github.root_LH.specnote.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.json.JsonParser;
import org.springframework.boot.json.JsonParserFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * 한국산업인력공단(Q-Net) API 호출
 * - 국가자격 시험일정 조회 (JSON)
 * - 국가자격 종목 목록 (XML)
 */
@Slf4j
@Component
public class QnetClient {

    private static final String QUAL_TYPE_NATIONAL_TECH = "T";   // 국가기술자격
    /** API 제한: 한 페이지 최대 50건 */
    private static final int MAX_ROWS = 50;
    private static final int LOG_BODY_LIMIT = 300;

    private final RestClient restClient;
    private final PublicDataProperties properties;
    private final JsonParser jsonParser = JsonParserFactory.getJsonParser();

    public QnetClient(@Qualifier("qnetRestClient") RestClient restClient, PublicDataProperties properties) {
        this.restClient = restClient;
        this.properties = properties;
    }

    public boolean isConfigured() {
        return properties.hasServiceKey();
    }

    // ===== 시험일정 =====

    /**
     * @param year 시행년도
     * @param jmCd 종목코드 (예: 1320 = 정보처리기사)
     */
    public List<QnetScheduleItem> fetchSchedules(int year, String jmCd) {
        URI uri = URI.create(properties.qnet().scheduleUrl()
                + "?serviceKey=" + properties.encodedServiceKey()
                + "&numOfRows=" + MAX_ROWS
                + "&pageNo=1"
                + "&dataFormat=json"
                + "&implYy=" + year
                + "&qualgbCd=" + QUAL_TYPE_NATIONAL_TECH
                + "&jmCd=" + URLEncoder.encode(jmCd, StandardCharsets.UTF_8));

        byte[] body = request(uri, "schedule year=" + year + " jmCd=" + jmCd);
        return QnetResponseParser.parse(toJsonMap(body));
    }

    // ===== 종목 목록 =====

    /** 국가자격 전체 종목 목록 (조건 없이 전체 조회) */
    public List<QnetExamItem> fetchExamCatalog() {
        URI uri = URI.create(properties.qnet().examListUrl()
                + "?serviceKey=" + properties.encodedServiceKey());

        byte[] body = request(uri, "exam-list");
        return QnetXmlParser.parseItems(body).stream()
                .map(QnetExamItem::from)
                .filter(QnetExamItem::isValid)
                .toList();
    }

    // ===== 공통 =====

    /**
     * 응답을 바이트 그대로 받음.
     * 문자열(String)로 받으면 서버가 charset을 알려주지 않을 때 스프링이 ISO-8859-1로 읽어서
     * 한글이 깨짐 (종목명·계열명 비교가 전부 실패하는 원인이었음).
     * → 바이트로 받아 JSON은 UTF-8로, XML은 파서가 선언된 인코딩으로 직접 읽게 함.
     */
    private byte[] request(URI uri, String label) {
        try {
            byte[] body = restClient.get().uri(uri).retrieve().body(byte[].class);
            return body == null ? new byte[0] : body;
        } catch (RestClientException e) {
            log.warn("[Q-Net API] 호출 실패 {} reason={}", label, e.getMessage());
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    private Map<String, Object> toJsonMap(byte[] bytes) {
        String body = new String(bytes, StandardCharsets.UTF_8).trim();
        if (body.startsWith("<")) {
            // 인증키 오류 등은 XML로 옴 → XML 파서가 원인을 로그로 남기고 예외를 던짐
            QnetXmlParser.parseItems(bytes);
            log.warn("[Q-Net API] JSON 대신 XML 응답 body={}", abbreviate(body));
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
        try {
            return jsonParser.parseMap(body);
        } catch (IllegalArgumentException e) {   // JsonParseException도 이 예외의 하위 클래스
            log.warn("[Q-Net API] JSON 해석 실패 body={}", abbreviate(body));
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    private static String abbreviate(String body) {
        return body.length() <= LOG_BODY_LIMIT ? body : body.substring(0, LOG_BODY_LIMIT) + "...";
    }
}
