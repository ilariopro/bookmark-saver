package com.example.bookmark_saver.controller;

import com.example.bookmark_saver.dto.common.ApiListResponse;
import com.example.bookmark_saver.dto.common.ApiResponse;
import com.example.bookmark_saver.dto.request.IdListRequest;
import com.example.bookmark_saver.dto.request.TagRequest;
import com.example.bookmark_saver.dto.response.TagResponse;
import com.example.bookmark_saver.service.TagService;
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
 * REST controller for managing tags.
 *
 * Exposes endpoints under {@code /api/tags} for CRUD operations
 * and bulk bookmark association updates.
 */
@RestController
@RequestMapping("/api/tags")
public class TagController {
    /**
     * Service for tag business logic.
     */
    private TagService service;

    /**
     * @param service The tag service.
     */
    public TagController(TagService service) {
        this.service = service;
    }

    /**
     * Returns all tags.
     * 
     * @return The complete list of {@link TagResponse}.
     */
    @GetMapping
    public ResponseEntity<ApiListResponse<TagResponse>> list() {
        return ResponseEntity.ok(
            ResponseFactory.list(service.findAll(), TagResponse::from)
        );
    }

    /**
     * Finds a tag by its ID.
     *
     * @param tagId The ID of the tag.
     * 
     * @return The matching {@link TagResponse}.
     */
    @GetMapping("/{tagId}")
    public ResponseEntity<ApiResponse<TagResponse>> get(
        @PathVariable Long tagId
    ) {
        return ResponseEntity.ok(
            ResponseFactory.one(service.findById(tagId), TagResponse::from)
        );
    }

    /**
     * Creates a new tag.
     *
     * @param request The tag creation request.
     * 
     * @return The created {@link TagResponse} with HTTP 201.
     */
    @PostMapping
    public ResponseEntity<ApiResponse<TagResponse>> create(
        @Valid @RequestBody TagRequest request
    ) {
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(ResponseFactory.one(service.save(request), TagResponse::from));
    }

    /**
     * Updates an existing tag.
     *
     * @param tagId   The ID of the tag to update.
     * @param request The update request.
     * 
     * @return The updated {@link TagResponse}.
     */
    @PutMapping("/{tagId}")
    public ResponseEntity<ApiResponse<TagResponse>> update(
        @PathVariable Long tagId,
        @Valid @RequestBody TagRequest request
    ) {
        return ResponseEntity.ok(
            ResponseFactory.one(service.update(tagId, request), TagResponse::from)
        );
    }

    /**
     * Replaces the bookmark associations of a tag.
     *
     * @param tagId   The ID of the tag.
     * @param request The request containing the new bookmark IDs.
     * 
     * @return The updated {@link TagResponse}.
     */
    @PutMapping("/{tagId}/bookmarks")
    public ResponseEntity<ApiResponse<TagResponse>> updateBookmarks(
        @PathVariable Long tagId,
        @RequestBody IdListRequest request
    ) {
        return ResponseEntity.ok(
            ResponseFactory.one(service.updateBookmarks(tagId, request.ids()), TagResponse::from)
        );
    }

    /**
     * Deletes a tag by its ID.
     *
     * @param tagId The ID of the tag to delete.
     * 
     * @return HTTP 204 No Content.
     */
    @DeleteMapping("/{tagId}")
    public ResponseEntity<Void> delete(
        @PathVariable Long tagId
    ) {
        service.delete(tagId);

        return ResponseEntity.noContent().build();
    }
}