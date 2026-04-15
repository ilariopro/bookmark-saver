package com.example.bookmark_saver.domain;

import java.time.Instant;

/**
 * Value object holding metadata extracted from a bookmark URL.
 * Embedded in {@link Bookmark} entity.
 */
public class Metadata {
    private String title;
    private String description;
    private String imageUrl;
    private String favicon;
    private String siteName;
    private String canonicalUrl;
    private String domain;
    private String contentType;
    private String extractedAt;

    public Metadata() {}

    public Metadata(
        String title,
        String description,
        String imageUrl,
        String favicon,
        String siteName,
        String canonicalUrl,
        String domain,
        String contentType
    ) {
        this.title = title;
        this.description = description;
        this.imageUrl = imageUrl;
        this.favicon = favicon;
        this.siteName = siteName;
        this.canonicalUrl = canonicalUrl;
        this.domain = domain;
        this.contentType = contentType;
        this.extractedAt = Instant.now().toString();
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

    public String getFavicon() {
        return this.favicon;
    }

    public String getSiteName() {
        return this.siteName;
    }

    public String getCanonicalUrl() {
        return this.canonicalUrl;
    }

    public String getDomain() {
        return this.domain;
    }

    public String getContentType() {
        return this.contentType;
    }

    public String getExtractedAt() {
        return this.extractedAt;
    }
}