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
     * // TODO remove this method and update tests
     *
     * @param name The name to search for.
     * 
     * @return An {@link Optional} containing the matching tag, or empty if not found.
     */
    Optional<Tag> findByName(String name);

    /**
     * Checks if a tag already exists.
     * 
     * @param parentId
     * @param name
     * 
     * @return
     */
    boolean existsByParentIdAndNameIgnoreCase(Long parentId, String name);

    /**
     * Finds all parent tags.
     * 
     * @param sort
     * 
     * @return
     */
    List<Tag> findByParentIsNull(Sort sort);
}