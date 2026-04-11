package com.example.bookmark_saver.controller;

import com.example.bookmark_saver.dto.common.PageInfo;
import com.example.bookmark_saver.dto.common.PagedResponse;
import com.example.bookmark_saver.dto.common.BasicResponse;
import com.example.bookmark_saver.dto.request.BookmarkRequest;
import com.example.bookmark_saver.dto.request.IdListRequest;
import com.example.bookmark_saver.dto.response.BookmarkResponse;
import com.example.bookmark_saver.service.BookmarkService;

import jakarta.validation.Valid;

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
     * @param tag      If non-blank, filters by tag name (case-insensitive).
     * @param pageable The pagination information.
     * 
     * @return A paged list of {@link BookmarkResponse}.
     */
    @GetMapping
    public ResponseEntity<PagedResponse<BookmarkResponse>> list(
        @RequestParam(required = false) Boolean favorite,
        @RequestParam(required = false) String tag,
        Pageable pageable
    ) {
        Page<BookmarkResponse> page = service
            .findAll(favorite, tag, pageable)
            .map(BookmarkResponse::from);

        return ResponseEntity.ok(
            PagedResponse.of(page.getContent(), PageInfo.from(page))
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
    public ResponseEntity<BasicResponse<BookmarkResponse>> get(
        @PathVariable Long bookmarkId
    ) {
        return ResponseEntity.ok(
            BasicResponse.of(BookmarkResponse.from(service.findById(bookmarkId)))
        );
    }

    /**
     * Creates a new bookmark.
     *
     * @param request The bookmark creation request.
     * @return The created {@link BookmarkResponse} with HTTP 201.
     */
    @PostMapping
    public ResponseEntity<BasicResponse<BookmarkResponse>> create(
        @Valid @RequestBody BookmarkRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(BasicResponse.of(BookmarkResponse.from(service.save(request))));
    }

    /**
     * Updates an existing bookmark.
     *
     * @param bookmarkId The ID of the bookmark to update.
     * @param request    The update request.
     * @return The updated {@link BookmarkResponse}.
     */
    @PutMapping("/{bookmarkId}")
    public ResponseEntity<BasicResponse<BookmarkResponse>> update(
        @PathVariable Long bookmarkId,
        @Valid @RequestBody BookmarkRequest request
    ) {
        return ResponseEntity.ok(
            BasicResponse.of(BookmarkResponse.from(service.update(bookmarkId, request)))
        );
    }

    /**
     * Replaces the tag associations of a bookmark.
     *
     * @param bookmarkId The ID of the bookmark.
     * @param request    The request containing the new tag IDs.
     * @return The updated {@link BookmarkResponse}.
     */
    @PutMapping("/{bookmarkId}/tags")
    public ResponseEntity<BasicResponse<BookmarkResponse>> updateTags(
        @PathVariable Long bookmarkId,
        @RequestBody IdListRequest request
    ) {
        return ResponseEntity.ok(
            BasicResponse.of(
                BookmarkResponse.from(service.updateTags(bookmarkId, request.ids()))
            )
        );
    }

    /**
     * Deletes a bookmark by its ID.
     *
     * @param bookmarkId The ID of the bookmark to delete.
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