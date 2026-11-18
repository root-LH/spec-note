package io.github.root_LH.specnote.exam.dto;

import io.github.root_LH.specnote.exam.domain.ExamStage;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * 날짜는 "2027-01-15" 형식. 시험일만 필수, 나머지는 미정이면 비워도 됨
 */
public record ScheduleCreateRequest(
        @NotBlank @Size(max = 50)
        String roundName,

        @NotNull
        ExamStage stage,

        LocalDate regStart,
        LocalDate regEnd,

        @NotNull
        LocalDate examDate,

        LocalDate resultDate
) {
}
