package com.example.bookmark_saver.service;

import com.example.bookmark_saver.domain.Tag;
import com.example.bookmark_saver.dto.request.TagRequest;
import com.example.bookmark_saver.repository.BookmarkRepository;
import com.example.bookmark_saver.repository.TagRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for managing tags and their associations with bookmarks.
 *
 * Provides CRUD operations on {@link Tag} entities and supports
 * bulk reassignment of bookmarks.
 */
@Service
public class TagService {
    /**
     * Repository for accessing and persisting tags.
     */
    private TagRepository tagRepository;

    /**
     * Repository for accessing and persisting bookmarks.
     */
    private BookmarkRepository bookmarkRepository;

    /**
     * JDBC template for executing batch updates.
     */
    private JdbcTemplate jdbcTemplate;

    /**
     * @param tagRepository      The tag repository.
     * @param bookmarkRepository The bookmark repository.
     * @param jdbcTemplate       The JDBC template for batch operations.
     */
    public TagService(
        TagRepository tagRepository,
        BookmarkRepository bookmarkRepository,
        JdbcTemplate jdbcTemplate
    ) {
        this.tagRepository = tagRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Returns all tags.
     * 
     * @param sort Sort options.
     * 
     * @return The complete list of {@link Tag}.
     */
    public List<Tag> findAll(Sort sort) {
        return tagRepository.findAll(sort);
    }

    /**
     * Finds a tag by its ID.
     *
     * @param tagId The ID of the tag.
     * 
     * @return The matching tag.
     * @throws EntityNotFoundException If no tag is found with the given ID.
     */
    public Tag findById(Long tagId) {
        return tagRepository
            .findById(tagId)
            .orElseThrow(
                () -> new EntityNotFoundException("Tag not found with id: " + tagId)
            );
    }

    /**
     * Creates a new tag.
     *
     * @param request The tag creation request.
     * 
     * @return The saved tag.
     */
    public Tag save(TagRequest request) {
        Tag tag = new Tag();
        
        tag.setName(normalizeName(request.name()));

        return tagRepository.save(tag);
    }

    /**
     * Updates an existing tag.
     * 
     * If a tag with the same name already exists, the associated bookmarks
     * are merged with it and the current tag is deleted.
     *
     * @param tagId   The ID of the tag to update.
     * @param request The update request containing the new name.
     * 
     * @return The updated or merged tag.
     * @throws EntityNotFoundException If no tag is found with the given ID.
     */
    @Transactional
    public Tag update(Long tagId, TagRequest request) {
        Tag tag = findById(tagId);

        String normalizedName = normalizeName(request.name());

        Tag existingTag = tagRepository
            .findByName(normalizedName)
            .orElse(null);

        if (existingTag != null && !existingTag.getId().equals(tagId)) {
            // 1) Eliminate possible duplicates
            jdbcTemplate.update(
                """
                DELETE FROM bookmark_tags old_relation
                WHERE old_relation.tag_id = ?
                AND EXISTS (
                    SELECT 1
                    FROM bookmark_tags new_relation
                    WHERE new_relation.bookmark_id = old_relation.bookmark_id
                    AND new_relation.tag_id = ?
                )
                """,
                existingTag.getId(),
                tagId
            );

            // 2) Move the remaining relationships
            jdbcTemplate.update(
                """
                UPDATE bookmark_tags
                SET tag_id = ?
                WHERE tag_id = ?
                """,
                tagId,
                existingTag.getId()
            );

            tagRepository.deleteById(existingTag.getId());

            return tag;
        }

        tag.setName(normalizedName);

        return tagRepository.save(tag);
    }

    /**
     * Replaces all bookmark associations for a tag.
     *
     * @param tagId       The ID of the tag.
     * @param bookmarkIds The IDs of the bookmarks to associate.
     * 
     * @return The updated tag with its new associations.
     * @throws EntityNotFoundException If no tag is found with the given ID.
     */
    @Transactional
    public Tag updateBookmarks(Long tagId, List<Long> bookmarkIds) {
        findById(tagId);

        bookmarkRepository.deleteAllByTagId(tagId);

        jdbcTemplate.batchUpdate(
            "INSERT INTO bookmark_tags (bookmark_id, tag_id) VALUES (?, ?)",
            bookmarkIds,
            bookmarkIds.size(),
            (statement, bookmarkId) -> {
                statement.setLong(1, bookmarkId);
                statement.setLong(2, tagId);
            }
        );

        return findById(tagId);
    }

    /**
     * Deletes a tag by its ID.
     *
     * @param tagId The ID of the tag to delete.
     * 
     * @throws EntityNotFoundException If no tag is found with the given ID.
     */
    public void delete(Long tagId) {
        findById(tagId);

        tagRepository.deleteById(tagId);
    }

    /**
     * Normalizes a tag name.
     */
    private String normalizeName(String name) {
        return name
            .trim()
            .replaceAll("\\s+", " ")
            .toLowerCase();
    }
}