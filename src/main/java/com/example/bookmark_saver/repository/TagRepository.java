package com.example.bookmark_saver.repository;

import com.example.bookmark_saver.domain.Tag;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link Tag} entities.
 *
 * Extends {@link JpaRepository} to provide standard CRUD and pagination operations.
 */
public interface TagRepository extends JpaRepository<Tag, Long> {
    /**
     * Finds a tag by its name.
     *
     * @param name The name to search for.
     * @return An {@link Optional} containing the matching {@link Tag}, or empty if not found.
     */
    Optional<Tag> findByName(String name);
}