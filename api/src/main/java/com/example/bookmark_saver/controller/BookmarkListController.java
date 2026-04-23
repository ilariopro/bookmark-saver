package com.example.bookmark_saver.controller;

import com.example.bookmark_saver.dto.request.IdListRequest;
import com.example.bookmark_saver.dto.common.ApiListResponse;
import com.example.bookmark_saver.dto.common.ApiResponse;
import com.example.bookmark_saver.dto.request.BookmarkListRequest;
import com.example.bookmark_saver.dto.response.BookmarkListResponse;
import com.example.bookmark_saver.service.BookmarkListService;
import com.example.bookmark_saver.utility.ResponseFactory;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for managing lists.
 *
 * Exposes endpoints under {@code /api/lists} for CRUD operations
 * and bulk bookmark association updates.
 */
@RestController
@RequestMapping("/api/lists")
public class BookmarkListController {
    /**
     * Service for list business logic.
     */
    private BookmarkListService service;

    /**
     * @param service The list service.
     */
    public BookmarkListController(BookmarkListService service) {
        this.service = service;
    }

    /**
     * Returns all lists.
     * 
     * @return The complete list of {@link BookmarkListResponse}.
     */
    @GetMapping
    public ResponseEntity<ApiListResponse<BookmarkListResponse>> list() {
        return ResponseEntity.ok(
            ResponseFactory.list(service.findAll(), BookmarkListResponse::from)
        );
    }

    /**
     * Finds a list by its ID.
     *
     * @param listId The ID of the list.
     * 
     * @return The matching {@link BookmarkListResponse}.
     */
    @GetMapping("/{listId}")
    public ResponseEntity<ApiResponse<BookmarkListResponse>> get(
        @PathVariable Long listId
    ) {
        return ResponseEntity.ok(
            ResponseFactory.one(service.findById(listId), BookmarkListResponse::from)
        );
    }

    /**
     * Creates a new list.
     *
     * @param request The list creation request.
     * 
     * @return The created {@link BookmarkListResponse} with HTTP 201.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<BookmarkListResponse>> create(
        @Valid @RequestBody BookmarkListRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ResponseFactory.one(service.save(request), BookmarkListResponse::from));
    }

    /**
     * Updates an existing list.
     *
     * @param listId  The ID of the list to update.
     * @param request The update request.
     * 
     * @return The updated {@link BookmarkListResponse}.
     */
    @PutMapping("/{listId}")
    public ResponseEntity<ApiResponse<BookmarkListResponse>> update(
        @PathVariable Long listId,
        @Valid @RequestBody BookmarkListRequest request
    ) {
        return ResponseEntity.ok(
            ResponseFactory.one(service.update(listId, request), BookmarkListResponse::from)
        );
    }

    /**
     * Replaces the bookmark associations of a list.
     *
     * @param listId  The ID of the list.
     * @param request The request containing the new bookmark IDs.
     * 
     * @return The updated {@link BookmarkListResponse}.
     */
    @PutMapping("/{listId}/bookmarks")
    public ResponseEntity<ApiResponse<BookmarkListResponse>> updateBookmarks(
        @PathVariable Long listId,
        @RequestBody IdListRequest request
    ) {
        return ResponseEntity.ok(
            ResponseFactory.one(service.updateBookmarks(listId, request.ids()), BookmarkListResponse::from)
        );
    }

    /**
     * Deletes a list by its ID.
     *
     * @param listId The ID of the list to delete.
     * 
     * @return HTTP 204 No Content.
     */
    @DeleteMapping("/{listId}")
    public ResponseEntity<Void> delete(
        @PathVariable Long listId
    ) {
        service.delete(listId);

        return ResponseEntity.noContent().build();
    }
}