package com.example.bookmark_saver.utility;

import java.util.List;
import java.util.function.Function;

import org.springframework.data.domain.Page;

import com.example.bookmark_saver.dto.common.ApiListResponse;
import com.example.bookmark_saver.dto.common.ApiResponse;
import com.example.bookmark_saver.dto.common.PageInfo;

/**
 * Provides helpers to map domain objects into API responses.
 */
public final class ResponseFactory {
    /**
     * Prevent instantiation.
     */
    private ResponseFactory() {}

    /**
     * Converts a single entity into a API response.
     *
     * @param <T>    The source type
     * @param <R>    The response type
     * @param data   The source object.
     * @param mapper Function to convert the object.
     * 
     * @return The API response.
     */
    public static <T, R> ApiResponse<R> one(T data, Function<T, R> mapper) {
        return ApiResponse.of(
            mapper.apply(data)
        );
    }

    /**
     * Converts a list of entities into a API list response.
     *
     * @param <T>    The source type
     * @param <R>    The response type
     * @param data   The list of source objects.
     * @param mapper The function to convert each element.
     * 
     * @return The API response containing mapped list.
     */
    public static <T, R> ApiListResponse<R> list(List<T> data, Function<T, R> mapper) {
        return ApiListResponse.of(
            data.stream().map(mapper).toList(),
            null
        );
    }

    /**
     * Converts a paginated result into a API list response with pagination metadata.
     *
     * @param <T>    The source type
     * @param <R>    The response type
     * @param data   The paginated source data.
     * @param mapper The function to convert each element.
     * 
     * @return The API response containing mapped data and pagination info.
     */
    public static <T, R> ApiListResponse<R> page(Page<T> data, Function<T, R> mapper) {
        return ApiListResponse.of(
            data.stream().map(mapper).toList(),
            PageInfo.from(data)
        );
    }
}