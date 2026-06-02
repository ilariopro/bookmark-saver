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
    Long parentId,
    Set<Long> bookmarkIds,
    String backgroundColor,
    String textColor,
    Instant createdAt,
    Instant updatedAt
) {
    public static TagResponse from(Tag tag) {
        Set<Long> bookmarkIds = tag.getBookmarks()
            .stream()
            .map(bookmark -> bookmark.getId())
            .collect(Collectors.toSet());

        Tag parent = tag.getParent();

        return new TagResponse(
            tag.getId(),
            tag.getName(),
            parent != null ? parent.getId() : null,
            bookmarkIds,
            tag.getBackgroundColor(),
            tag.getTextColor(),
            tag.getCreatedAt(),
            tag.getUpdatedAt()
        );
    }
}
