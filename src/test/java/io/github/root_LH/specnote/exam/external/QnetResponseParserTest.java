package io.github.root_LH.specnote.exam.external;

import io.github.root_LH.specnote.exam.domain.ExamStage;
import io.github.root_LH.specnote.global.exception.BusinessException;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * 실제 API를 호출하지 않고, 응답 모양(Map)만으로 파싱 로직 검증.
 * 샘플 값은 실제 응답(2026년 정보처리기사 1회)을 사용.
 */
class QnetResponseParserTest {

    private static Map<String, Object> sampleItem(String regStart, String regEnd) {
        Map<String, Object> item = new HashMap<>();
        item.put("implYy", "2026");
        item.put("implSeq", 1);   // 실제 응답처럼 숫자로 와도 처리되는지
        item.put("description", "국가기술자격 기사 (2026년도 제1회)");
        item.put("docRegStartDt", regStart);
        item.put("docRegEndDt", regEnd);
        item.put("docExamStartDt", "20260130");
        item.put("docExamEndDt", "20260303");
        item.put("docPassDt", "20260311");
        item.put("pracRegStartDt", "20260323");
        item.put("pracRegEndDt", "20260413");
        item.put("pracExamStartDt", "20260418");
        item.put("pracExamEndDt", "20260506");
        item.put("pracPassDt", "20260605");
        return item;
    }

    private static Map<String, Object> sampleItem() {
        return sampleItem("20260112", "20260115");
    }

    private static Map<String, Object> success(Object items) {
        return Map.of(
                "header", Map.of("resultCode", "00", "resultMsg", "NORMAL SERVICE"),
                "body", Map.of("items", items, "totalCount", 1));
    }

    @Test
    void items가_배열이면_그대로_파싱한다() {
        List<QnetScheduleItem> result = QnetResponseParser.parse(success(List.of(sampleItem())));

        assertThat(result).hasSize(1);
        QnetScheduleItem item = result.get(0);
        assertThat(item.roundName()).isEqualTo("2026년 1회");
        assertThat(item.docRegStart()).isEqualTo(LocalDate.of(2026, 1, 12));
        assertThat(item.pracPass()).isEqualTo(LocalDate.of(2026, 6, 5));
    }

    @Test
    void response로_감싸고_item이_객체_하나여도_파싱한다() {
        Map<String, Object> root = Map.of("response", success(Map.of("item", sampleItem())));

        assertThat(QnetResponseParser.parse(root)).hasSize(1);
    }

    @Test
    void 결과가_없으면_빈_목록() {
        assertThat(QnetResponseParser.parse(success(""))).isEmpty();
    }

    @Test
    void 데이터_없음_코드는_오류가_아니라_빈_목록() {
        Map<String, Object> root = Map.of("header", Map.of("resultCode", "03", "resultMsg", "NODATA_ERROR"));

        assertThat(QnetResponseParser.parse(root)).isEmpty();
    }

    @Test
    void 그_외_오류_코드면_예외() {
        Map<String, Object> root = Map.of("header", Map.of("resultCode", "30", "resultMsg", "SERVICE KEY IS NOT REGISTERED ERROR."));

        assertThatThrownBy(() -> QnetResponseParser.parse(root)).isInstanceOf(BusinessException.class);
    }

    @Test
    void 필기와_실기_단계로_나눈다() {
        QnetScheduleItem item = QnetScheduleItem.from(sampleItem());

        assertThat(item.stages())
                .extracting(QnetScheduleItem.StageDates::stage)
                .containsExactly(ExamStage.WRITTEN, ExamStage.PRACTICAL);
        assertThat(item.stages().get(0).examDate()).isEqualTo(LocalDate.of(2026, 1, 30));
    }

    @Test
    void 실기_날짜가_전부_없으면_필기만_남는다() {
        Map<String, Object> raw = sampleItem();
        raw.put("pracRegStartDt", "");
        raw.put("pracRegEndDt", "");
        raw.put("pracExamStartDt", "");
        raw.put("pracPassDt", "");

        assertThat(QnetScheduleItem.from(raw).stages())
                .extracting(QnetScheduleItem.StageDates::stage)
                .containsExactly(ExamStage.WRITTEN);
    }

    @Test
    void 같은_회차가_두_건이면_접수가_가장_빠른_것만_남긴다() {
        // 실제 응답: 1회 필기 접수가 01.24~01.25(추가)와 01.12~01.15(정기) 두 건으로 옴
        List<QnetScheduleItem> items = List.of(
                QnetScheduleItem.from(sampleItem("20260124", "20260125")),
                QnetScheduleItem.from(sampleItem("20260112", "20260115")));

        List<QnetScheduleItem> merged = QnetScheduleItem.mergeSameRound(items);

        assertThat(merged).hasSize(1);
        assertThat(merged.get(0).docRegStart()).isEqualTo(LocalDate.of(2026, 1, 12));
        assertThat(merged.get(0).docRegEnd()).isEqualTo(LocalDate.of(2026, 1, 15));
    }
}
