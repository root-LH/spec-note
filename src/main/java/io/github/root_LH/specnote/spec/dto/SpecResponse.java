package io.github.root_LH.specnote.spec.dto;

import io.github.root_LH.specnote.exam.domain.ExamCategory;
import io.github.root_LH.specnote.spec.domain.MemberSpec;
import io.github.root_LH.specnote.spec.domain.SpecStatus;

import java.time.LocalDate;

public record SpecResponse(
        Long specId,
        Long examId,
        String name,
        ExamCategory category,
        String score,
        String certNumber,
        LocalDate acquiredDate,
        LocalDate expireDate,
        Long daysUntilExpire,     // 영구면 null, 지났으면 음수
        SpecStatus status,
        String statusName
) {

    public static SpecResponse of(MemberSpec spec, LocalDate today) {
        SpecStatus status = spec.status(today);
        return new SpecResponse(
                spec.getId(),
                spec.getExam() != null ? spec.getExam().getId() : null,
                spec.displayName(),
                spec.getExam() != null ? spec.getExam().getCategory() : null,
                spec.getScore(),
                spec.getCertNumber(),
                spec.getAcquiredDate(),
                spec.getExpireDate(),
                spec.daysUntilExpire(today),
                status,
                status.getDescription());
    }
}
