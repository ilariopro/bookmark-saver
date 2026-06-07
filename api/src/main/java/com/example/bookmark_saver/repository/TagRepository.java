package com.example.bookmark_saver.repository;

import com.example.bookmark_saver.domain.Tag;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link Tag} entities.
 *
 * Extends {@link JpaRepository} to provide standard CRUD and sort operations.
 */
public interface TagRepository extends JpaRepository<Tag, Long> {
    /**
     * Finds a tag by slug.
     *
     * @param slug The slug to search for.
     * 
     * @return An {@link Optional} containing the matching tag, or empty if not found.
     */
    Optional<Tag> findBySlug(String slug);

    /**
     * Finds all parent tags.
     * 
     * @param sort
     * 
     * @return A {@link List} containing all parent tags.
     */
    List<Tag> findByParentIsNull(Sort sort);
}