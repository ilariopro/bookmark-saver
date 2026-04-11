package com.example.bookmark_saver.domain;

import jakarta.persistence.Embeddable;

/**
 * Value object holding metadata extracted from a bookmark URL.
 * Embedded in {@link Bookmark} entity.
 */
@Embeddable
public class Metadata {
    private String title;

    private String description;
    
    private String imageUrl;

    public Metadata() {}

    public static Metadata create(
        String title,
        String description,
        String imageUrl
    ) {
        Metadata metadata = new Metadata();

        metadata.title = title;
        metadata.description = description;
        metadata.imageUrl = imageUrl;

        return metadata;
    }

    public String getTitle() {
        return this.title;
    }

    public String getDescription() {
        return this.description;
    }

    public String getImageUrl() {
        return this.imageUrl;
    }
}