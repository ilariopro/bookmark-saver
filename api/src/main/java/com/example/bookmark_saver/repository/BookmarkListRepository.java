package com.example.bookmark_saver.repository;

import com.example.bookmark_saver.domain.BookmarkList;

import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for {@link BookmarkList} entities.
 *
 * Extends {@link JpaRepository} to provide standard CRUD and pagination operations.
 */
public interface BookmarkListRepository extends JpaRepository<BookmarkList, Long> {}