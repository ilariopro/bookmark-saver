package com.example.bookmark_saver.dto.request;

import java.util.List;

import com.example.bookmark_saver.validation.OnCreate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Request payload for creating or updating a bookmark.
 */
public record BookmarkRequest(
    @NotBlank(groups = OnCreate.class)
    @Pattern(regexp = "https?://.*", message = "URL must be valid")
    String url,

    String notes,

    Boolean favorite,

    List<Long> listIds,

    List<Long> tagIds
) {}