package com.example.bookmark_saver.repository;

import com.example.bookmark_saver.domain.Tag;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link Tag} entities.
 *
 * Extends {@link JpaRepository} to provide standard CRUD and sort operations.
 */
public interface TagRepository extends JpaRepository<Tag, Long> {
    /**
     * Determines if a tag already exists.
     * 
     * @param slug The slug to search for.
     * 
     * @return True if the tag already exists.
     */
    boolean existsBySlug(String slug);
}