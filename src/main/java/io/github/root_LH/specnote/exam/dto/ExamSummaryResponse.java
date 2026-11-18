package io.github.root_LH.specnote.exam.dto;

import io.github.root_LH.specnote.exam.domain.Exam;
import io.github.root_LH.specnote.exam.domain.ExamCategory;

/**
 * 시험 목록용 응답
 */
public record ExamSummaryResponse(
        Long examId,
        String name,
        ExamCategory category,
        String categoryName,
        String organizer,
        Integer validityMonths
) {

    public static ExamSummaryResponse from(Exam exam) {
        return new ExamSummaryResponse(
                exam.getId(),
                exam.getName(),
                exam.getCategory(),
                exam.getCategory().getDescription(),
                exam.getOrganizer(),
                exam.getValidityMonths());
    }
}
