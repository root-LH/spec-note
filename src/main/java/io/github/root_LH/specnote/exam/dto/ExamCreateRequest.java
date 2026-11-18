package io.github.root_LH.specnote.exam.dto;

import io.github.root_LH.specnote.exam.domain.ExamCategory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ExamCreateRequest(
        @Size(max = 20)
        String examCode,

        @NotBlank @Size(max = 100)
        String name,

        @NotNull
        ExamCategory category,

        @Size(max = 100)
        String organizer,

        @Min(1) @Max(120)
        Integer validityMonths,   // 비우면 영구

        @Size(max = 300)
        String officialUrl
) {
}
