package io.github.root_LH.specnote.exam.external;

import java.util.Map;
import java.util.stream.Stream;

/**
 * 종목 목록 API의 item 하나
 *
 * @param jmCd       종목코드 (예: 1320)
 * @param name       종목명 (예: 정보처리기사)
 * @param qualGbCd   자격구분 (T: 국가기술자격, S: 국가전문자격)
 * @param seriesName 계열명 (예: 기술사, 기능장, 기사, 기능사)
 */
public record QnetExamItem(String jmCd, String name, String qualGbCd, String seriesName) {

    private static final String ENGINEER_SERIES = "기사";

    /** 키는 QnetXmlParser가 소문자로 통일해서 넘겨줌 */
    public static QnetExamItem from(Map<String, String> item) {
        return new QnetExamItem(
                item.get("jmcd"),
                firstNonBlank(item.get("jmfldnm"), item.get("jmnm")),   // 종목명 태그가 API마다 다름
                item.get("qualgbcd"),
                item.get("seriesnm"));
    }

    public boolean isValid() {
        return jmCd != null && !jmCd.isBlank() && name != null && !name.isBlank();
    }

    /**
     * 자격 등급.
     * Q-Net의 "기사" 계열(seriesCd 03)에는 기사·산업기사와 사회조사분석사 같은 다른 종목이 함께 들어 있어서
     * 계열명만으로는 구분이 안 됨 → 종목명으로 한 번 더 나눔.
     *
     * @return 기사 / 산업기사 / 기사계열기타 / (그 외 계열은 계열명 그대로: 기술사, 기능장, 기능사 …)
     */
    public String grade() {
        if (!ENGINEER_SERIES.equals(seriesName)) {
            return seriesName;
        }
        if (name.contains("산업기사")) {
            return "산업기사";
        }
        if (name.contains("기사")) {
            return "기사";   // 소방설비기사(기계분야)처럼 괄호가 붙은 이름도 포함
        }
        return "기사계열기타";   // 사회조사분석사1급, 직업상담사2급 등
    }

    private static String firstNonBlank(String... values) {
        return Stream.of(values)
                .filter(value -> value != null && !value.isBlank())
                .findFirst()
                .orElse(null);
    }
}
