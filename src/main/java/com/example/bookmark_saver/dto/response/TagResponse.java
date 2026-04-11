package com.example.bookmark_saver.dto.response;

import com.example.bookmark_saver.domain.Tag;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Response payload representing a tag.
 */
public record TagResponse(
    Long id,
    String name,
    Set<BookmarkResponse> bookmarks,
    Instant createdAt,
    Instant updatedAt
) {
    public static TagResponse from(Tag tag) {
        Set<BookmarkResponse> bookmarks = tag.getBookmarks()
            .stream()
            .map(BookmarkResponse::from)
            .collect(Collectors.toSet());

        return new TagResponse(
            tag.getId(),
            tag.getName(),
            bookmarks,
            tag.getCreatedAt(),
            tag.getUpdatedAt()
        );
    }
}
