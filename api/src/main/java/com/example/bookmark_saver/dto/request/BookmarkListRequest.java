package com.example.bookmark_saver.dto.request;

import com.example.bookmark_saver.validation.OnCreate;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for creating or updating a list.
 */
public record BookmarkListRequest(
    @NotBlank(groups = OnCreate.class) String name,
    String description
) {}