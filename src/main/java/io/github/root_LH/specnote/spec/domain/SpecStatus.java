package io.github.root_LH.specnote.spec.domain;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 오늘 기준 스펙 상태 (DB에 저장하지 않고 매번 계산)
 */
@Getter
@RequiredArgsConstructor
public enum SpecStatus {
    PERMANENT("영구"),
    ACTIVE("유효"),
    EXPIRING_SOON("만료 임박"),
    EXPIRED("만료");

    private final String description;
}
