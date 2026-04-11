package com.example.bookmark_saver.dto.request;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for creating or updating a tag.
 */
public record TagRequest(
    @NotBlank String name
) {}