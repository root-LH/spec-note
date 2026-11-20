package io.github.root_LH.specnote.exam.external;

import io.github.root_LH.specnote.global.exception.BusinessException;
import io.github.root_LH.specnote.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 공공데이터 API 응답(JSON)에서 item 목록을 꺼냄.
 *
 * 공공데이터 API는 기관마다 응답 모양이 조금씩 달라서 아래 경우를 모두 처리:
 * - { "response": { "header": ..., "body": ... } } 처럼 response로 한 번 감싼 경우 / 안 감싼 경우
 * - items가 배열인 경우 / { "item": [...] } 인 경우 / 결과가 1건이라 item이 배열이 아닌 객체인 경우
 * - 결과가 없을 때 오류 코드(03 NODATA)로 응답하는 경우 → 빈 목록으로 처리
 *
 * 특정 JSON 라이브러리 클래스에 의존하지 않도록 Map으로 받아서 직접 탐색함.
 */
@Slf4j
public final class QnetResponseParser {

    private static final String SUCCESS_CODE = "00";
    /** 공공데이터포털 공통 코드: 03 = 데이터 없음 */
    private static final Set<String> NO_DATA_CODES = Set.of("03");

    private QnetResponseParser() {
    }

    public static List<QnetScheduleItem> parse(Map<String, ?> root) {
        if (root == null) {
            return List.of();
        }
        Map<String, ?> container = root.containsKey("response") ? asMap(root.get("response")) : root;

        Map<String, ?> header = asMap(container.get("header"));
        Object resultCode = header.get("resultCode");
        if (resultCode != null) {
            String code = String.valueOf(resultCode);
            String message = String.valueOf(header.get("resultMsg"));
            if (NO_DATA_CODES.contains(code) || message.toUpperCase().contains("NODATA")) {
                return List.of();   // 아직 공고되지 않은 연도 등
            }
            if (!SUCCESS_CODE.equals(code)) {
                log.warn("[Q-Net API] 오류 응답 resultCode={} resultMsg={}", code, message);
                throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
            }
        }

        Map<String, ?> body = asMap(container.get("body"));
        return extractItems(body.get("items")).stream()
                .map(QnetScheduleItem::from)
                .toList();
    }

    private static List<Map<String, ?>> extractItems(Object items) {
        if (items instanceof List<?> list) {
            return list.stream().<Map<String, ?>>map(QnetResponseParser::asMap).toList();
        }
        if (items instanceof Map<?, ?> map) {
            Object item = map.get("item");
            if (item instanceof List<?> list) {
                return list.stream().<Map<String, ?>>map(QnetResponseParser::asMap).toList();
            }
            if (item instanceof Map<?, ?>) {
                return List.of(asMap(item));
            }
        }
        return List.of();   // 결과 0건이면 items가 비어 있거나 ""로 옴
    }

    @SuppressWarnings("unchecked")
    private static Map<String, ?> asMap(Object value) {
        return value instanceof Map<?, ?> map ? (Map<String, ?>) map : Map.of();
    }
}
