package com.example.bookmark_saver.dto.response;

import java.util.List;

import com.example.bookmark_saver.domain.Tag;

/**
 * Response payload representing the complete tag tree.
 */
public record TagTreeResponse(
    Long id,
    String name,
    String slug,
    String backgroundColor,
    String textColor,
    Long parentId,
    List<TagTreeResponse> children
) {
    public static TagTreeResponse from(Tag tag) {
        return new TagTreeResponse(
            tag.getId(),
            tag.getName(),
            tag.getSlug(),
            tag.getBackgroundColor(),
            tag.getTextColor(),
            tag.getParent() != null ? tag.getParent().getId() : null,
            tag.getChildren()
                .stream()
                .map(TagTreeResponse::from)
                .toList()
        );
    }
}
