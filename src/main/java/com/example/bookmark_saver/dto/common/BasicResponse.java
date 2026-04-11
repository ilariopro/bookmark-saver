package com.example.bookmark_saver.dto.common;

/**
 * Generic response wrapper for a single result.
 */
public record BasicResponse<T>(
    T data
) {
    public static <T> BasicResponse<T> of(T data) {
        return new BasicResponse<>(data);
    }
}
