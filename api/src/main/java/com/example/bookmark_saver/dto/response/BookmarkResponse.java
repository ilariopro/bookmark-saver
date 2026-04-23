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
    Set<BookmarkListResponse> lists,
    Set<TagResponse> tags,
    Metadata metadata,
    MetadataStatus metadataStatus,
    Instant createdAt,
    Instant updatedAt
) {
    public static BookmarkResponse from(Bookmark bookmark) {
        Set<BookmarkListResponse> lists = bookmark.getLists()
            .stream()
            .map(BookmarkListResponse::from)
            .collect(Collectors.toSet());

        Set<TagResponse> tags = bookmark.getTags()
            .stream()
            .map(TagResponse::from)
            .collect(Collectors.toSet());

        return new BookmarkResponse(
            bookmark.getId(),
            bookmark.getUrl(),
            bookmark.getNotes(),
            bookmark.isFavorite(),
            lists,
            tags,
            bookmark.getMetadata(),
            bookmark.getMetadataStatus(),
            bookmark.getCreatedAt(),
            bookmark.getUpdatedAt()
        );
    }
}

