package io.github.root_LH.specnote.exam.dto;

import io.github.root_LH.specnote.exam.domain.Exam;
import io.github.root_LH.specnote.exam.domain.ExamCategory;
import io.github.root_LH.specnote.exam.domain.ExamSource;

import java.util.List;

/**
 * 시험 상세 + 다가오는 회차 목록
 */
public record ExamDetailResponse(
        Long examId,
        String examCode,
        String name,
        ExamCategory category,
        String categoryName,
        String organizer,
        Integer validityMonths,
        String officialUrl,
        ExamSource source,
        List<ScheduleResponse> upcomingSchedules
) {

    public static ExamDetailResponse of(Exam exam, List<ScheduleResponse> upcomingSchedules) {
        return new ExamDetailResponse(
                exam.getId(),
                exam.getExamCode(),
                exam.getName(),
                exam.getCategory(),
                exam.getCategory().getDescription(),
                exam.getOrganizer(),
                exam.getValidityMonths(),
                exam.getOfficialUrl(),
                exam.getSource(),
                upcomingSchedules);
    }
}
