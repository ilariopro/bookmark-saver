package com.example.bookmark_saver.dto.response;

import com.example.bookmark_saver.domain.BookmarkList;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Response payload representing a list.
 */
public record BookmarkListResponse(
    Long id,
    String name,
    String description,
    Set<Long> bookmarkIds,
    Instant createdAt,
    Instant updatedAt
) {
    public static BookmarkListResponse from(BookmarkList list) {
        Set<Long> bookmarkIds = list.getBookmarks()
            .stream()
            .map(bookmark -> bookmark.getId())
            .collect(Collectors.toSet());

        return new BookmarkListResponse(
            list.getId(),
            list.getName(),
            list.getDescription(),
            bookmarkIds,
            list.getCreatedAt(),
            list.getUpdatedAt()
        );
    }
}
