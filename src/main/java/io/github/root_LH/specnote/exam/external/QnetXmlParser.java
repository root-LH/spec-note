package io.github.root_LH.specnote.exam.external;

import io.github.root_LH.specnote.global.exception.BusinessException;
import io.github.root_LH.specnote.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * XML로만 응답하는 Q-Net API용 파서 (자바 기본 XML 파서 사용, 라이브러리 추가 없음).
 *
 * - 바이트 그대로 받아서 파싱 → XML 선언의 인코딩(없으면 UTF-8)으로 읽어 한글이 깨지지 않음
 * - 모든 &lt;item&gt;의 하위 태그를 Map으로 변환 (태그 이름은 소문자로 통일 → jmCd, jmcd 차이 무시)
 * - 공공데이터포털 공통 오류 응답(OpenAPI_ServiceResponse)과 resultCode 오류를 감지
 * - XXE(외부 엔티티) 공격 방지 설정 적용
 */
@Slf4j
public final class QnetXmlParser {

    private static final String SUCCESS_CODE = "00";

    private QnetXmlParser() {
    }

    public static List<Map<String, String>> parseItems(String xml) {
        return parseItems(xml.getBytes(StandardCharsets.UTF_8));
    }

    public static List<Map<String, String>> parseItems(byte[] xml) {
        Document document = parse(xml);
        Element root = document.getDocumentElement();

        // 인증키 오류 등 포털 공통 오류 형식
        if ("OpenAPI_ServiceResponse".equals(root.getTagName())) {
            log.warn("[Q-Net API] 포털 오류 응답 reason={} message={}",
                    text(root, "returnAuthMsg"), text(root, "errMsg"));
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }

        String resultCode = text(root, "resultCode");
        if (resultCode != null && !SUCCESS_CODE.equals(resultCode)) {
            log.warn("[Q-Net API] 오류 응답 resultCode={} resultMsg={}", resultCode, text(root, "resultMsg"));
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }

        NodeList itemNodes = root.getElementsByTagName("item");
        List<Map<String, String>> items = new ArrayList<>(itemNodes.getLength());
        for (int i = 0; i < itemNodes.getLength(); i++) {
            items.add(toMap((Element) itemNodes.item(i)));
        }
        return items;
    }

    private static Document parse(byte[] xml) {
        try {
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            // XXE 방지: DOCTYPE과 외부 엔티티를 막아 서버 파일 읽기·내부망 요청 공격 차단
            factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
            factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            factory.setXIncludeAware(false);
            factory.setExpandEntityReferences(false);

            return factory.newDocumentBuilder().parse(new InputSource(new ByteArrayInputStream(xml)));
        } catch (ParserConfigurationException | SAXException | IOException e) {
            log.warn("[Q-Net API] XML 해석 실패 reason={}", e.getMessage());
            throw new BusinessException(ErrorCode.EXTERNAL_API_ERROR);
        }
    }

    private static Map<String, String> toMap(Element item) {
        Map<String, String> map = new HashMap<>();
        NodeList children = item.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            Node child = children.item(i);
            if (child instanceof Element element) {
                map.put(element.getTagName().toLowerCase(Locale.ROOT), element.getTextContent().trim());
            }
        }
        return map;
    }

    private static String text(Element root, String tagName) {
        NodeList nodes = root.getElementsByTagName(tagName);
        return nodes.getLength() == 0 ? null : nodes.item(0).getTextContent().trim();
    }
}
