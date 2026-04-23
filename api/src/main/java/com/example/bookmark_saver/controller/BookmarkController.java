package com.example.bookmark_saver.controller;

import com.example.bookmark_saver.domain.Bookmark;
import com.example.bookmark_saver.dto.common.ApiListResponse;
import com.example.bookmark_saver.dto.common.ApiResponse;
import com.example.bookmark_saver.dto.request.BookmarkRequest;
import com.example.bookmark_saver.dto.request.IdListRequest;
import com.example.bookmark_saver.dto.response.BookmarkResponse;
import com.example.bookmark_saver.service.BookmarkService;
import com.example.bookmark_saver.utility.CommaSeparatedParser;
import com.example.bookmark_saver.utility.ResponseFactory;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing bookmarks.
 *
 * Exposes endpoints under {@code /api/bookmarks} for CRUD operations,
 * dynamic filtering, and bulk tag association updates.
 */
@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {
    /**
     * Service for bookmark business logic.
     */
    private BookmarkService service;

    /**
     * @param service The bookmark service.
     */
    public BookmarkController(BookmarkService service) {
        this.service = service;
    }

    /**
     * Returns a paginated list of bookmarks, optionally filtered.
     *
     * @param favorite If non-null, filters by favorite status.
     * @param list     // TODO add support for list filtering
     * @param tags     If non-blank, filters by tag names (case-insensitive).
     * @param pageable The pagination information.
     * 
     * @return A paged list of {@link BookmarkResponse}.
     */
    @GetMapping
    public ResponseEntity<ApiListResponse<BookmarkResponse>> list(
        @RequestParam(required = false) Boolean favorite,
        @RequestParam(required = false) String tags,
        Pageable pageable
    ) {
        List<String> parsedTags = CommaSeparatedParser.parse(tags);
        Page<Bookmark> bookmarks = service.findAll(favorite, parsedTags, pageable);

        return ResponseEntity.ok(
            ResponseFactory.page(bookmarks, BookmarkResponse::from)
        );
    }

    /**
     * Finds a bookmark by its ID.
     *
     * @param bookmarkId The ID of the bookmark.
     * 
     * @return The matching {@link BookmarkResponse}.
     */
    @GetMapping("/{bookmarkId}")
    public ResponseEntity<ApiResponse<BookmarkResponse>> get(
        @PathVariable Long bookmarkId
    ) {
        return ResponseEntity.ok(
            ResponseFactory.one(service.findById(bookmarkId), BookmarkResponse::from)
        );
    }

    /**
     * Creates a new bookmark.
     *
     * @param request The bookmark creation request.
     * 
     * @return The created {@link BookmarkResponse} with HTTP 201.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BookmarkResponse>> create(
        @Valid @RequestBody BookmarkRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ResponseFactory.one(service.save(request), BookmarkResponse::from));
    }

    /**
     * Updates an existing bookmark.
     *
     * @param bookmarkId The ID of the bookmark to update.
     * @param request    The update request.
     * 
     * @return The updated {@link BookmarkResponse}.
     */
    @PutMapping("/{bookmarkId}")
    public ResponseEntity<ApiResponse<BookmarkResponse>> update(
        @PathVariable Long bookmarkId,
        @Valid @RequestBody BookmarkRequest request
    ) {
        return ResponseEntity.ok(
            ResponseFactory.one(service.update(bookmarkId, request), BookmarkResponse::from)
        );
    }

    /**
     * Replaces the list associations of a bookmark.
     *
     * @param bookmarkId The ID of the bookmark.
     * @param request    The request containing the new list IDs.
     * 
     * @return The updated {@link BookmarkResponse}.
     */
    @PutMapping("/{bookmarkId}/lists")
    public ResponseEntity<ApiResponse<BookmarkResponse>> updateLists(
        @PathVariable Long bookmarkId,
        @RequestBody IdListRequest request
    ) {
        return ResponseEntity.ok(
            ResponseFactory.one(service.updateLists(bookmarkId, request.ids()), BookmarkResponse::from)
        );
    }

    /**
     * Replaces the tag associations of a bookmark.
     *
     * @param bookmarkId The ID of the bookmark.
     * @param request    The request containing the new tag IDs.
     * 
     * @return The updated {@link BookmarkResponse}.
     */
    @PutMapping("/{bookmarkId}/tags")
    public ResponseEntity<ApiResponse<BookmarkResponse>> updateTags(
        @PathVariable Long bookmarkId,
        @RequestBody IdListRequest request
    ) {
        return ResponseEntity.ok(
            ResponseFactory.one(service.updateTags(bookmarkId, request.ids()), BookmarkResponse::from)
        );
    }

    /**
     * Deletes a bookmark by its ID.
     *
     * @param bookmarkId The ID of the bookmark to delete.
     * 
     * @return HTTP 204 No Content.
     */
    @DeleteMapping("/{bookmarkId}")
    public ResponseEntity<Void> delete(
        @PathVariable Long bookmarkId
    ) {
        service.delete(bookmarkId);
 
        return ResponseEntity.noContent().build();
    }
}