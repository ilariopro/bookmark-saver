package com.example.bookmark_saver.service;

import com.example.bookmark_saver.domain.Metadata;
import com.example.bookmark_saver.domain.MetadataStatus;
import com.example.bookmark_saver.repository.BookmarkRepository;

import java.io.IOException;
import java.net.URI;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service for extracting and enriching metadata from bookmark URLs.
 * 
 * Enrichment is executed asynchronously via the {@code metadataExecutor} thread pool.
 * Fetches HTML content via {@code Jsoup}.
 */
@Service
public class MetadataService {
    /**
     * Repository for accessing and persisting bookmarks.
     */
    private BookmarkRepository repository;

    /**
     * Logger for reporting metadata extraction failures.
     */
    private static final Logger logger = LoggerFactory.getLogger(MetadataService.class);

    /**
     * @param repository The bookmark repository.
     */
    public MetadataService(BookmarkRepository repository) {
        this.repository = repository;
    }

    /**
     * Asynchronously enriches a bookmark's metadata by its ID.
     * 
     * @param bookmarkId The ID of the bookmark to enrich.
     */
    @Async("metadataExecutor")
    public void enrich(Long bookmarkId) {
        repository.findById(bookmarkId).ifPresent(bookmark -> {
            bookmark.setMetadataStatus(MetadataStatus.PENDING);

            try {
                Metadata metadata = extract(bookmark.getUrl());

                bookmark.setMetadata(metadata);
                bookmark.setMetadataStatus(MetadataStatus.SUCCESS);
            } catch (Exception e) {
                logger.warn("Metadata enrichment failed for bookmark {}", bookmarkId, e);

                bookmark.setMetadataStatus(MetadataStatus.FAILED);
            }

            repository.save(bookmark);
        });
    }

    /**
     * Extracts metadata from the given URL.
     *
     * @param url The target URL.
     * 
     * @return The extracted {@link Metadata}, or an empty object on failure.
     * @throws RuntimeException If metadata extraction fails.
     */
    @Retryable(
        retryFor = Exception.class,
        maxAttempts = 3,
        backoff = @Backoff(delay = 1000)
    )
    public Metadata extract(String url) {
        try {
            Document doc = connect(url);

            String title = firstNonBlank(
                content(doc, "meta[property=og:title]"),
                doc.title()
            );

            String description = firstNonBlank(
                content(doc, "meta[property=og:description]"),
                content(doc, "meta[name=twitter:description]"),
                content(doc, "meta[name=description]")
            );

            if (description == null || description.length() < 20) {
                description = firstParagraph(doc);
            }

            String imageUrl = firstNonBlank(
                absolute(doc, "meta[property=og:image]", "content"),
                absolute(doc, "meta[name=twitter:image]", "content")
            );

            String favicon = firstNonBlank(
                absolute(doc, "link[rel~=icon]", "href"),
                absolute(doc, "link[rel='shortcut icon']", "href")
            );

            return new Metadata(
                title,
                description,
                imageUrl,
                favicon,
                content(doc, "meta[property=og:site_name]"),
                absolute(doc, "link[rel=canonical]", "href"),
                URI.create(url).getHost(),
                doc.connection().response().contentType()
            );
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Opens an HTTP connection to the given URL and returns the parsed HTML document.
     *
     * Configures user agent, referrer, timeout, and redirect handling to improve
     * compatibility with modern websites.
     *
     * @param url The target URL to fetch.
     *
     * @return The parsed HTML {@link Document}.
     * @throws IOException If the connection fails or the response cannot be parsed.
     */
    private Document connect(String url) throws IOException {
        return Jsoup.connect(url)
            .userAgent("Mozilla/5.0 BookmarkSaverBot/1.0")
            .referrer("https://www.google.com")
            .timeout(5000)
            .followRedirects(true)
            .get();
    }

    /**
     * Extracts the value of the {@code content} attribute from the first element.
     *
     * @param doc The parsed HTML document.
     * @param selector The CSS selector used to locate the element.
     *
     * @return The attribute value, or {@code null} if no matching element is found.
     */
    private String content(Document doc, String selector) {
        Element element = doc.selectFirst(selector);

        return element != null
            ? element.attr("content")
            : null;
    }

    /**
     * Extracts an absolute URL from the specified attribute of the first element.
     *
     * Relative URLs are automatically resolved against the document base URL.
     *
     * @param doc The parsed HTML document.
     * @param selector The CSS selector used to locate the element.
     * @param attribute The attribute containing the URL value.
     *
     * @return The resolved absolute URL, or {@code null} if no matching element is found.
     */
    private String absolute(Document doc, String selector, String attribute) {
        Element element = doc.selectFirst(selector);

        return element != null
            ? element.absUrl(attribute)
            : null;
    }

    /**
     * Returns the first non-null and non-blank value from the given candidates.
     *
     * @param values Candidate values to evaluate in order.
     *
     * @return The first meaningful value, or {@code null} if none is found.
     */
    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (value != null && !value.isBlank()) {
                return value.trim();
            }
        }
        
        return null;
    }

    /**
     * Extracts the first paragraph from the article body.
     *
     * @param doc The parsed HTML document.
     *
     * @return The first paragraph if it is long enough, otherwise {@code null}.
     */
    private String firstParagraph(Document doc) {
        return doc.select("article p").stream()
            .map(element -> element.text())
            .filter(text -> text.length() >= 20)
            .findFirst()
            .orElse(null);
    }
}