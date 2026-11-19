package io.github.root_LH.specnote.spec.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * 스펙 등록·수정 공통 요청
 * - examId 또는 customName 중 하나 필수 (목록에 없는 자격증은 customName)
 * - expireDate를 비우면 시험의 유효기간으로 자동 계산
 */
public record SpecRequest(
        Long examId,

        @Size(max = 100)
        String customName,

        @Size(max = 20)
        String score,

        @Size(max = 50)
        String certNumber,

        @NotNull @PastOrPresent(message = "취득일은 오늘 이전이어야 합니다.")
        LocalDate acquiredDate,

        LocalDate expireDate
) {
}
