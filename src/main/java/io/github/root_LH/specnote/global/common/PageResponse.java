package io.github.root_LH.specnote.global.common;

import org.springframework.data.domain.Page;

import java.util.List;

/**
 * 페이지 응답 공통 형식.
 * Spring의 Page 객체를 그대로 반환하면 불필요한 내부 필드가 많고 형식이 버전마다 바뀔 수 있어 직접 정의.
 */
public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean hasNext
) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.hasNext());
    }
}
