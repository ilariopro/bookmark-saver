package com.example.bookmark_saver.repository;

import com.example.bookmark_saver.domain.Bookmark;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

/**
 * Repository for {@link Bookmark} entities.
 *
 * Extends {@link JpaRepository} and {@link JpaSpecificationExecutor} to support
 * standard CRUD, pagination, and dynamic filtering via JPA Specifications.
 */
public interface BookmarkRepository extends JpaRepository<Bookmark, Long>, JpaSpecificationExecutor<Bookmark> {
    /**
     * Deletes all bookmark-list associations for the given list ID.
     *
     * @param listId The ID of the list whose associations should be removed.
     */
    @Modifying
    @Query(
        value = "DELETE FROM bookmark_lists WHERE list_id = :listId",
        nativeQuery = true
    )
    void deleteAllByListId(Long listId);

    /**
     * Deletes all bookmark-tag associations for the given tag ID.
     *
     * @param tagId The ID of the tag whose associations should be removed.
     */
    @Modifying
    @Query(
        value = "DELETE FROM bookmark_tags WHERE tag_id = :tagId",
        nativeQuery = true
    )
    void deleteAllByTagId(Long tagId);
}