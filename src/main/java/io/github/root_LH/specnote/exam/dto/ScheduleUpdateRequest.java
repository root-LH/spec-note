package io.github.root_LH.specnote.exam.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * 회차 날짜 수정. 회차명·단계는 중복 기준(UNIQUE)이라 바꾸지 않고, 바꾸려면 삭제 후 다시 등록.
 */
public record ScheduleUpdateRequest(
        LocalDate regStart,
        LocalDate regEnd,

        @NotNull
        LocalDate examDate,

        LocalDate resultDate
) {
}
