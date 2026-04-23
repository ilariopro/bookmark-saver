package com.example.bookmark_saver.dto.common;

import java.util.List;

/**
 * Generic response wrapper for listed results.
 */
public record ApiListResponse<T>(
    List<T> data,
    PageInfo meta
) {
    public static <T> ApiListResponse<T> of(List<T> data, PageInfo page) {
        return new ApiListResponse<>(data, page);
    }
}
