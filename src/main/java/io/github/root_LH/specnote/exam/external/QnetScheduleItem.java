package io.github.root_LH.specnote.exam.external;

import io.github.root_LH.specnote.exam.domain.ExamStage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * API 응답의 item 하나 = 한 회차의 필기 + 실기 일정
 */
public record QnetScheduleItem(
        String implYy,          // 시행년도
        String implSeq,         // 시행회차
        String description,     // 예: 국가기술자격 기사 (2026년도 제1회)
        LocalDate docRegStart,  // 필기 원서접수 시작
        LocalDate docRegEnd,
        LocalDate docExamStart, // 필기 시험 시작
        LocalDate docPass,      // 필기 합격 발표
        LocalDate pracRegStart, // 실기 원서접수 시작
        LocalDate pracRegEnd,
        LocalDate pracExamStart,
        LocalDate pracPass
) {

    /** 한 단계(필기 또는 실기)의 날짜 묶음 */
    public record StageDates(ExamStage stage, LocalDate regStart, LocalDate regEnd,
                             LocalDate examDate, LocalDate resultDate) {

        boolean isEmpty() {
            return regStart == null && regEnd == null && examDate == null && resultDate == null;
        }
    }

    /** "20260113" 형식 */
    private static final DateTimeFormatter API_DATE = DateTimeFormatter.BASIC_ISO_DATE;

    public static QnetScheduleItem from(Map<String, ?> item) {
        return new QnetScheduleItem(
                text(item, "implYy"),
                text(item, "implSeq"),
                text(item, "description"),
                date(item, "docRegStartDt"),
                date(item, "docRegEndDt"),
                date(item, "docExamStartDt"),
                date(item, "docPassDt"),
                date(item, "pracRegStartDt"),
                date(item, "pracRegEndDt"),
                date(item, "pracExamStartDt"),
                date(item, "pracPassDt"));
    }

    /**
     * 같은 회차가 여러 건 오면(정기 접수 + 추가 접수 등) 필기 접수 시작이 가장 빠른 것 하나만 남김.
     * → 동기화할 때마다 두 건이 번갈아 덮어써서 "변경됨"으로 잡히는 문제 방지
     */
    public static List<QnetScheduleItem> mergeSameRound(Collection<QnetScheduleItem> items) {
        Comparator<QnetScheduleItem> earliestRegistration = Comparator.comparing(
                QnetScheduleItem::docRegStart, Comparator.nullsLast(Comparator.naturalOrder()));

        Map<String, QnetScheduleItem> byRound = items.stream()
                .sorted(earliestRegistration)
                .collect(Collectors.toMap(
                        QnetScheduleItem::roundName,
                        item -> item,
                        (first, second) -> first,   // 정렬했으므로 먼저 들어온 것이 가장 빠른 접수
                        LinkedHashMap::new));
        return List.copyOf(byRound.values());
    }

    /** 우리 DB의 회차명. 동기화할 때 같은 회차를 찾는 기준이라 형식을 바꾸면 안 됨 */
    public String roundName() {
        return "%s년 %s회".formatted(implYy, implSeq);
    }

    /** 날짜가 하나라도 있는 단계만 반환 (실기가 없는 회차도 있음) */
    public List<StageDates> stages() {
        return Stream.of(
                        new StageDates(ExamStage.WRITTEN, docRegStart, docRegEnd, docExamStart, docPass),
                        new StageDates(ExamStage.PRACTICAL, pracRegStart, pracRegEnd, pracExamStart, pracPass))
                .filter(stage -> !stage.isEmpty())
                .toList();
    }

    private static String text(Map<String, ?> item, String key) {
        Object value = item.get(key);
        return value == null ? null : String.valueOf(value).trim();
    }

    /** 숫자(20260113)로 와도, 빈 문자열이어도 처리 */
    private static LocalDate date(Map<String, ?> item, String key) {
        String value = text(item, key);
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return LocalDate.parse(value, API_DATE);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
