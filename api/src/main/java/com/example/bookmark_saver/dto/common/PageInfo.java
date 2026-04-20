package com.example.bookmark_saver.dto.common;

import org.springframework.data.domain.Page;

/**
 * Pagination metadata extracted from a {@link Page}.
 */
public record PageInfo(
    long total,
    int size,
    int page,
    int pages,
    boolean next,
    boolean previous
) {
    public static PageInfo from(Page<?> page) {
        return new PageInfo(
            page.getTotalElements(),
            page.getSize(),
            page.getNumber(),
            page.getTotalPages(),
            page.hasNext(),
            page.hasPrevious()
        );
    }
}
