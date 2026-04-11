package com.example.bookmark_saver.dto.common;

import java.util.List;

/**
 * Generic response wrapper for paginated results.
 */
public record PagedResponse<T>(
    List<T> data,
    PageInfo meta
) {
    public static <T> PagedResponse<T> of(List<T> data, PageInfo page) {
        return new PagedResponse<>(data, page);
    }
}
