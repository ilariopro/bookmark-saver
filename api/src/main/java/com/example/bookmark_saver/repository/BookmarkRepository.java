package com.example.bookmark_saver.repository;

import com.example.bookmark_saver.domain.Bookmark;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

/**
 * Repository for {@link Bookmark} entities.
 *
 * Extends {@link JpaRepository} and {@link JpaSpecificationExecutor} to support
 * standard CRUD, pagination, and dynamic filtering via JPA Specifications.
 */
public interface BookmarkRepository extends JpaRepository<Bookmark, Long>, JpaSpecificationExecutor<Bookmark> {}