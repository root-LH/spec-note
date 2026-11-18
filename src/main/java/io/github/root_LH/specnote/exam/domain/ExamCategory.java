package io.github.root_LH.specnote.exam.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExamCategory {
    NATIONAL("국가자격"),
    LANGUAGE("어학"),
    PRIVATE("민간자격");

    private final String description;
}
