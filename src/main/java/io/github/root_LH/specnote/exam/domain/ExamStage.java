package io.github.root_LH.specnote.exam.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ExamStage {
    WRITTEN("필기"),
    PRACTICAL("실기"),
    SINGLE("단일");   // 토익처럼 필기·실기 구분이 없는 시험

    private final String description;
}
