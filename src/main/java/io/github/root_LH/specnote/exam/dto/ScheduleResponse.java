package io.github.root_LH.specnote.exam.dto;

import io.github.root_LH.specnote.exam.domain.Exam;
import io.github.root_LH.specnote.exam.domain.ExamCategory;
import io.github.root_LH.specnote.exam.domain.ExamSchedule;
import io.github.root_LH.specnote.exam.domain.ExamStage;
import io.github.root_LH.specnote.exam.domain.RegistrationStatus;

import java.time.LocalDate;

public record ScheduleResponse(
        Long scheduleId,
        Long examId,
        String examName,
        ExamCategory category,
        String roundName,
        ExamStage stage,
        String stageName,
        LocalDate regStart,
        LocalDate regEnd,
        LocalDate examDate,
        LocalDate resultDate,
        RegistrationStatus registrationStatus,
        String registrationStatusName,
        boolean synced          // 공공데이터로 동기화된 일정이면 true (다음 동기화 때 날짜가 갱신될 수 있음)
) {

    /**
     * @param today 접수 상태 계산 기준일
     */
    public static ScheduleResponse of(ExamSchedule schedule, LocalDate today) {
        Exam exam = schedule.getExam();
        RegistrationStatus status = schedule.registrationStatus(today);
        return new ScheduleResponse(
                schedule.getId(),
                exam.getId(),
                exam.getName(),
                exam.getCategory(),
                schedule.getRoundName(),
                schedule.getStage(),
                schedule.getStage().getDescription(),
                schedule.getRegStart(),
                schedule.getRegEnd(),
                schedule.getExamDate(),
                schedule.getResultDate(),
                status,
                status.getDescription(),
                schedule.getSyncedAt() != null);
    }
}
