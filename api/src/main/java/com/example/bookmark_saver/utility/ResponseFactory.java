package com.example.bookmark_saver.utility;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

import com.example.bookmark_saver.dto.common.ApiListResponse;
import com.example.bookmark_saver.dto.common.ApiResponse;
import com.example.bookmark_saver.dto.common.PageInfo;

/**
 * // TODO add description
 */
public final class ResponseFactory {
    /**
     * Prevent instantiation.
     */
    private ResponseFactory() {}

    public static <T, R> ApiResponse<R> one(T data, Function<T, R> mapper) {
        return ApiResponse.of(
            mapper.apply(data)
        );
    }

    public static <T, R> ApiListResponse<R> list(List<T> data, Function<T, R> mapper) {
        return ApiListResponse.of(
            data.stream().map(mapper).toList(),
            null
        );
    }

    public static <T, R> ApiListResponse<R> page(Page<T> data, Function<T, R> mapper) {
        return ApiListResponse.of(
            data.stream().map(mapper).toList(),
            PageInfo.from(data)
        );
    }
}