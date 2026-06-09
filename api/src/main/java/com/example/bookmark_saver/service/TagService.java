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
     * @throws IllegalArgumentException If a tag with the specified name already exists.
     */
    public Tag save(TagRequest request) {
        if (tagRepository.existsBySlug(request.slug())) {
            throw new IllegalArgumentException("Tag already exists");
        }

        Tag tag = new Tag();
        
        tag.setName(request.name());
        tag.setSlug(request.slug());

        return tagRepository.save(tag);
    }

    /**
     * Updates an existing tag.
     *
     * @param tagId   The ID of the tag to update.
     * @param request The update request.
     * 
     * @return The updated tag.
     * @throws IllegalArgumentException If a tag with the specified name already exists.
     * @throws EntityNotFoundException If no tag is found with the given ID.
     */
    public Tag update(Long tagId, TagRequest request) {
        if (tagRepository.existsBySlug(request.slug())) {
            throw new IllegalArgumentException("Tag already exists");
        }

        Tag tag = findById(tagId);

        String name = request.name() != null
            ? request.name()
            : tag.getName();

        String slug = request.slug() != null
            ? request.slug()
            : tag.getSlug();

        tag.setName(name);
        tag.setSlug(slug);

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
}