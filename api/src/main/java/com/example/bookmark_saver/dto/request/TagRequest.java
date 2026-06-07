package com.example.bookmark_saver.dto.request;

import com.example.bookmark_saver.validation.OnCreate;

import jakarta.validation.constraints.NotBlank;

/**
 * Request payload for creating or updating a tag.
 */
public record TagRequest(
    @NotBlank(groups = OnCreate.class)
    String name,

    @NotBlank(groups = OnCreate.class)
    String slug,

    Long parentId,
    
    String backgroundColor,

    String textColor
) {}