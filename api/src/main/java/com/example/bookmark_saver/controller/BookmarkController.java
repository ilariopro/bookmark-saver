package com.example.bookmark_saver.controller;

import com.example.bookmark_saver.domain.Bookmark;
import com.example.bookmark_saver.dto.common.ApiListResponse;
import com.example.bookmark_saver.dto.common.ApiResponse;
import com.example.bookmark_saver.dto.request.BookmarkBulkDeleteRequest;
import com.example.bookmark_saver.dto.request.BookmarkBulkUpdateRequest;
import com.example.bookmark_saver.dto.request.BookmarkRequest;
import com.example.bookmark_saver.dto.response.BookmarkResponse;
import com.example.bookmark_saver.service.BookmarkService;
import com.example.bookmark_saver.utility.CommaSeparatedParser;
import com.example.bookmark_saver.utility.ResponseFactory;
import com.example.bookmark_saver.validation.OnCreate;
import com.example.bookmark_saver.validation.OnUpdate;

import jakarta.validation.Valid;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing bookmarks.
 *
 * Exposes endpoints under {@code /api/bookmarks} for CRUD operations.
 */
@RestController
@RequestMapping("/api/bookmarks")
public class BookmarkController {
    /**
     * Service for bookmark business logic.
     */
    @Autowired
    private BookmarkService service;

    /**
     * Returns a paginated list of bookmarks, optionally filtered.
     *
     * @param favorite If non-null, filters by favorite status.
     * @param archived If non-null, filters by archived status.
     * @param untagged If non-null, filters by untagged bookmarks.
     * @param tagIds   If non-blank, filters by tag ids.
     * @param pageable Pagination options.
     * 
     * @return A paged list of {@link BookmarkResponse}.
     */
    @GetMapping
    public ResponseEntity<ApiListResponse<BookmarkResponse>> list(
        @RequestParam(required = false) Boolean favorite,
        @RequestParam(required = false) Boolean archived,
        @RequestParam(required = false) Boolean untagged,
        @RequestParam(required = false) String tagIds,
        Pageable pageable
    ) {
        List<Long> parsedTagIds  = parseCommaSeparatedIds(tagIds);

        Page<Bookmark> bookmarks = service.findAll(
            favorite,
            archived,
            untagged,
            parsedTagIds,
            pageable
        );

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
            ResponseFactory.item(service.findById(bookmarkId), BookmarkResponse::from)
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
        @Validated(OnCreate.class) @RequestBody BookmarkRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ResponseFactory.item(service.save(request), BookmarkResponse::from));
    }

    /**
     * Updates an existing bookmark.
     *
     * @param bookmarkId The ID of the bookmark to update.
     * @param request    The update request.
     * 
     * @return The updated {@link BookmarkResponse}.
     */
    @PatchMapping("/{bookmarkId}")
    public ResponseEntity<ApiResponse<BookmarkResponse>> update(
        @PathVariable Long bookmarkId,
        @Validated(OnUpdate.class) @RequestBody BookmarkRequest request
    ) {
        return ResponseEntity.ok(
            ResponseFactory.item(service.update(bookmarkId, request), BookmarkResponse::from)
        );
    }

    /**
     * Updates a list of bookmarks.
     * 
     * @param request The update request.
     * 
     * @return HTTP 204 No Content.
     */
    @PatchMapping("/bulk")
    public ResponseEntity<Void> bulkUpdate(
        @Valid @RequestBody BookmarkBulkUpdateRequest request
    ) {
        service.bulkUpdate(request);

        return ResponseEntity.noContent().build();
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

    /**
     * Delets a list of bookmarks.
     * 
     * @param request The delete request.
     * 
     * @return HTTP 204 No Content.
     */
    @DeleteMapping("/bulk")
    public ResponseEntity<Void> bulkDelete(
        @Valid @RequestBody BookmarkBulkDeleteRequest request
    ) {
        service.bulkDelete(request.ids());

        return ResponseEntity.noContent().build();
    }

    /**
     * Parses comma-separated IDs into a list of Long.
     */
    private List<Long> parseCommaSeparatedIds(String ids) {
        return CommaSeparatedParser.parse(ids)
            .stream()
            .map(Long::valueOf)
            .toList();
    }
}