package io.github.root_LH.specnote.exam.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 오늘 날짜 기준 접수 상태 (DB에 저장하지 않고 매번 계산)
 */
@Getter
@RequiredArgsConstructor
public enum RegistrationStatus {
    BEFORE("접수 예정"),
    OPEN("접수 중"),
    CLOSED("접수 마감"),
    UNKNOWN("접수 일정 미정");

    private final String description;
}
