package com.example.bookmark_saver.dto.common;

/**
 * Generic response wrapper for a single result.
 */
public record ApiResponse<T>(
    T data
) {
    public static <T> ApiResponse<T> of(T data) {
        return new ApiResponse<>(data);
    }
}
