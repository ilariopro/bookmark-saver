package com.example.bookmark_saver.dto.response;

import com.example.bookmark_saver.domain.Bookmark;
import com.example.bookmark_saver.domain.Metadata;
import com.example.bookmark_saver.domain.MetadataStatus;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Response payload representing a bookmark.
 */
public record BookmarkResponse(
    Long id,
    String url,
    String notes,
    Boolean favorite,
    Set<TagResponse> tags,
    Metadata metadata,
    MetadataStatus metadataStatus,
    Instant createdAt,
    Instant updatedAt
) {
    public static BookmarkResponse from(Bookmark bookmark) {
        Set<TagResponse> tags = bookmark.getTags()
            .stream()
            .map(tag -> new TagResponse(
                tag.getId(),
                tag.getName(),
                null,
                tag.getCreatedAt(),
                tag.getUpdatedAt()
            ))
            .collect(Collectors.toSet());

        return new BookmarkResponse(
            bookmark.getId(),
            bookmark.getUrl(),
            bookmark.getNotes(),
            bookmark.isFavorite(),
            tags,
            bookmark.getMetadata(),
            bookmark.getMetadataStatus(),
            bookmark.getCreatedAt(),
            bookmark.getUpdatedAt()
        );
    }
}

