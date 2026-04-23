package com.example.bookmark_saver.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for creating or updating a list.
 */
public record BookmarkListRequest(
    @NotBlank String name,
    String description
) {}