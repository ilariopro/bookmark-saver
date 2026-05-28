package com.example.bookmark_saver.dto.request;

import java.util.List;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/**
 * Request payload for updating bookmarks in bulk.
 */
public record BookmarkBulkUpdateRequest(
    @NotNull
    @NotEmpty
    List<Long> ids,

    Boolean favorite,
    Boolean archived,

    List<Long> addTagIds,
    List<Long> removeTagIds
) {}