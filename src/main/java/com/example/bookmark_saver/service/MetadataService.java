package com.example.bookmark_saver.service;

import com.example.bookmark_saver.domain.Bookmark;
import com.example.bookmark_saver.domain.Metadata;
import com.example.bookmark_saver.repository.BookmarkRepository;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service for extracting and enriching metadata from bookmark URLs.
 * 
 * Fetches HTML content via {@code Jsoup} and parses title, description, and image.
 * Enrichment is executed asynchronously via the {@code metadataExecutor} thread pool.
 */
@Service
public class MetadataService {
    /**
     * Repository for accessing and persisting bookmarks.
     */
    private BookmarkRepository repository;

    /**
     * @param repository The bookmark repository.
     */
    public MetadataService(BookmarkRepository repository) {
        this.repository = repository;
    }

    /**
     * Extracts metadata (title, description, og:image) from the given URL.
     * Returns an empty {@link Metadata} on failure.
     *
     * @param url The target URL.
     * 
     * @return The extracted {@link Metadata}.
     */
    public Metadata extract(String url) {
        try {
            Document document = Jsoup.connect(url).get();
            
            return Metadata.create(
                document.title(),
                document.select("meta[name=description]").attr("content"),
                document.select("meta[property=og:image]").attr("content")
            );
        } catch (Exception exception) {
            return new Metadata();
        }
    }

    /**
     * Asynchronously enriches a bookmark's metadata by its ID.
     * 
     * @param bookmarkId The ID of the bookmark to enrich.
     */
    @Async("metadataExecutor")
    public void enrich(Long bookmarkId) {
        Bookmark bookmark = repository
            .findById(bookmarkId)
            .orElseThrow();

        Metadata metadata = extract(bookmark.getUrl());

        bookmark.setMetadata(metadata);

        repository.save(bookmark);
    }
}