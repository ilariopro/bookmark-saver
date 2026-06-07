package com.example.bookmark_saver.service;

import com.example.bookmark_saver.domain.Tag;
import com.example.bookmark_saver.dto.request.TagRequest;
import com.example.bookmark_saver.repository.TagRepository;

import jakarta.persistence.EntityNotFoundException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
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
    @Autowired
    private TagRepository tagRepository;

    /**
     * Returns all tags.
     * 
     * @param sort Sort options.
     * 
     * @return The complete list of {@link Tag}.
     */
    public List<Tag> findAll(Sort sort) {
        return tagRepository.findByParentIsNull(sort);
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
     * @throws IllegalArgumentException If a tag with the specified name already exists for this parent.
     * @throws EntityNotFoundException If no parent tag is found with the given ID.
     */
    public Tag save(TagRequest request) {
        checTagConflicts(request.slug());

        Tag tag = new Tag();
        
        tag.setName(request.name());
        tag.setSlug(request.slug());
        tag.setBackgroundColor(request.backgroundColor());
        tag.setTextColor(request.textColor());

        if (request.parentId() != null) {
            Tag parent = findById(request.parentId());

            tag.setParent(parent);
        }

        return tagRepository.save(tag);
    }

    /**
     * Updates an existing tag.
     *
     * @param tagId   The ID of the tag to update.
     * @param request The update request.
     * 
     * @return The updated tag.
     * @throws IllegalArgumentException If a tag with the specified name already exists for this parent.
     * @throws EntityNotFoundException If no tag is found with the given ID.
     */
    public Tag update(Long tagId, TagRequest request) {
        checTagConflicts(request.slug());

        Tag tag = findById(tagId);

        String name = request.name() != null
            ? request.name()
            : tag.getName();

        String slug = request.slug() != null
            ? request.slug()
            : tag.getSlug();

        String backgroundColor = request.backgroundColor() != null
            ? request.backgroundColor()
            : tag.getBackgroundColor();

        String textColor = request.textColor() != null
            ? request.textColor()
            : tag.getTextColor();

        Tag parent = request.parentId() != null
            ? findById(request.parentId())
            : null;

        tag.setName(name);
        tag.setSlug(slug);
        tag.setParent(parent);
        tag.setBackgroundColor(backgroundColor);
        tag.setTextColor(textColor);

        return tagRepository.save(tag);
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
     * Makes sure there are no tag conflicts.
     * 
     * @throws IllegalArgumentException If a tag with the specified slug already exists for this parent.
     */
    private void checTagConflicts(String slug) {
        if (tagRepository.findBySlug(slug).isPresent()) {
            throw new IllegalArgumentException(
                "Tag already exists for the slug " + slug
            );
        }
    }
}