package com.example.bookmark_saver.dto.common;

import java.util.List;

/**
 * Generic response wrapper for paginated results.
 */
public record PagedResponse<T>(
    PageInfo meta,
    List<T> data
) {
    public static <T> PagedResponse<T> of(List<T> data, PageInfo page) {
        return new PagedResponse<>(page, data);
    }
}
