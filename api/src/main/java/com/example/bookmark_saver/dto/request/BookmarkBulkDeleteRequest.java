package com.example.bookmark_saver.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for deleting bookmarks in bulk.
 */
public record BookmarkBulkDeleteRequest(
    @NotNull
    @NotEmpty
    List<Long> ids
) {}
