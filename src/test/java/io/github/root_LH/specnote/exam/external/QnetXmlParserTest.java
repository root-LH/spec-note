package io.github.root_LH.specnote.exam.external;

import io.github.root_LH.specnote.global.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 실제 응답(종목 목록 API)과 같은 형식으로 검증
 */
class QnetXmlParserTest {

    private static final String CATALOG = """
            <?xml version="1.0" encoding="UTF-8"?>
            <response>
              <header><resultCode>00</resultCode><resultMsg>NORMAL SERVICE.</resultMsg></header>
              <body><items>
                <item>
                  <jmcd>1320</jmcd><jmfldnm>정보처리기사</jmfldnm>
                  <qualgbcd>T</qualgbcd><seriescd>03</seriescd><seriesnm>기사</seriesnm>
                </item>
                <item>
                  <jmCd>2290</jmCd><jmFldNm>정보처리산업기사</jmFldNm>
                  <qualgbCd>T</qualgbCd><seriesNm>기사</seriesNm>
                </item>
              </items></body>
            </response>
            """;

    @Test
    void item을_소문자_키_Map으로_변환한다() {
        List<Map<String, String>> items = QnetXmlParser.parseItems(CATALOG);

        assertThat(items).hasSize(2);
        assertThat(items.get(0)).containsEntry("jmcd", "1320").containsEntry("seriesnm", "기사");
        // 태그 대소문자가 달라도 같은 키로 읽힘
        assertThat(items.get(1)).containsEntry("jmcd", "2290").containsEntry("jmfldnm", "정보처리산업기사");
    }

    @Test
    void 바이트로_받아도_한글이_깨지지_않는다() {
        // 실제 HTTP 응답처럼 UTF-8 바이트로 전달
        byte[] bytes = CATALOG.getBytes(StandardCharsets.UTF_8);

        assertThat(QnetXmlParser.parseItems(bytes).get(0)).containsEntry("jmfldnm", "정보처리기사");
    }

    @Test
    void 기사_계열은_종목명으로_등급을_나눈다() {
        assertThat(item("정보처리기사").grade()).isEqualTo("기사");
        assertThat(item("소방설비기사(기계분야)").grade()).isEqualTo("기사");
        assertThat(item("정보처리산업기사").grade()).isEqualTo("산업기사");
        assertThat(item("사회조사분석사1급").grade()).isEqualTo("기사계열기타");
    }

    @Test
    void 다른_계열은_계열명이_등급이다() {
        QnetExamItem engineer = new QnetExamItem("0601", "정보관리기술사", "T", "기술사");

        assertThat(engineer.grade()).isEqualTo("기술사");
    }

    @Test
    void 포털_공통_오류_응답이면_예외() {
        String error = """
                <OpenAPI_ServiceResponse><cmmMsgHeader>
                  <errMsg>SERVICE ERROR</errMsg>
                  <returnAuthMsg>SERVICE_KEY_IS_NOT_REGISTERED_ERROR</returnAuthMsg>
                  <returnReasonCode>30</returnReasonCode>
                </cmmMsgHeader></OpenAPI_ServiceResponse>
                """;

        assertThatThrownBy(() -> QnetXmlParser.parseItems(error)).isInstanceOf(BusinessException.class);
    }

    @Test
    void DOCTYPE이_있는_XML은_거부한다_XXE_방지() {
        String attack = """
                <?xml version="1.0"?>
                <!DOCTYPE foo [<!ENTITY xxe SYSTEM "file:///etc/passwd">]>
                <response><body><items><item><jmcd>&xxe;</jmcd></item></items></body></response>
                """;

        assertThatThrownBy(() -> QnetXmlParser.parseItems(attack)).isInstanceOf(BusinessException.class);
    }

    private static QnetExamItem item(String name) {
        return new QnetExamItem("0000", name, "T", "기사");
    }
}
