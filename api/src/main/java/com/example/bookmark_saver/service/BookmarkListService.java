package com.example.bookmark_saver.service;

import com.example.bookmark_saver.domain.BookmarkList;
import com.example.bookmark_saver.dto.request.BookmarkListRequest;
import com.example.bookmark_saver.repository.BookmarkListRepository;
import com.example.bookmark_saver.repository.BookmarkRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Service for managing lists and their associations with bookmarks.
 *
 * Provides CRUD operations on {@link BookmarkList} entities and supports
 * bulk reassignment of bookmarks.
 */
@Service
public class BookmarkListService {
    /**
     * Repository for accessing and persisting lists.
     */
    private BookmarkListRepository listRepository;

    /**
     * Repository for accessing and persisting bookmarks.
     */
    private BookmarkRepository bookmarkRepository;

    /**
     * JDBC template for executing batch updates.
     */
    private JdbcTemplate jdbcTemplate;

    /**
     * @param listRepository      The list repository.
     * @param bookmarkRepository The bookmark repository.
     * @param jdbcTemplate       The JDBC template for batch operations.
     */
    public BookmarkListService(
        BookmarkListRepository listRepository,
        BookmarkRepository bookmarkRepository,
        JdbcTemplate jdbcTemplate
    ) {
        this.listRepository = listRepository;
        this.bookmarkRepository = bookmarkRepository;
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Returns the complete list of lists.
     * 
     * @return The complete of list entities.
     */
    public List<BookmarkList> findAll() {
        return listRepository.findAll();
    }

    /**
     * Finds a list by its ID.
     *
     * @param listId The ID of the list.
     * 
     * @return The matching list.
     * @throws EntityNotFoundException If no list is found with the given ID.
     */
    public BookmarkList findById(Long listId) {
        return listRepository
            .findById(listId)
            .orElseThrow(
                () -> new EntityNotFoundException("List not found with id: " + listId)
            );
    }

    /**
     * Creates a new list.
     *
     * @param request The list creation request.
     * 
     * @return The saved list.
     */
    public BookmarkList save(BookmarkListRequest request) {
        BookmarkList list = new BookmarkList();
        
        list.setName(normalizeName(request.name()));
        list.setDescription(request.description());

        return listRepository.save(list);
    }

    /**
     * Updates an existing list.
     * 
     * If a list with the same name already exists, the associated bookmarks
     * are merged with it and the current list is deleted.
     *
     * @param listId  The ID of the list to update.
     * @param request The update request containing the new name.
     * 
     * @return The updated or merged list.
     * @throws EntityNotFoundException If no list is found with the given ID.
     */
    @Transactional
    public BookmarkList update(Long listId, BookmarkListRequest request) {
        BookmarkList list = findById(listId);

        list.setName(normalizeName(request.name()));
        list.setDescription(request.description());

        return listRepository.save(list);
    }

    /**
     * Replaces all bookmark associations for a list.
     *
     * @param listId      The ID of the list.
     * @param bookmarkIds The IDs of the bookmarks to associate.
     * 
     * @return The updated list with its new associations.
     * @throws EntityNotFoundException If no list is found with the given ID.
     */
    @Transactional
    public BookmarkList updateBookmarks(Long listId, List<Long> bookmarkIds) {
        findById(listId);

        bookmarkRepository.deleteAllByListId(listId);

        jdbcTemplate.batchUpdate(
            "INSERT INTO bookmark_lists (bookmark_id, list_id) VALUES (?, ?)",
            bookmarkIds,
            bookmarkIds.size(),
            (statement, bookmarkId) -> {
                statement.setLong(1, bookmarkId);
                statement.setLong(2, listId);
            }
        );

        return findById(listId);
    }

    /**
     * Deletes a list by its ID.
     *
     * @param listId The ID of the list to delete.
     * 
     * @throws EntityNotFoundException If no list is found with the given ID.
     */
    public void delete(Long listId) {
        findById(listId);

        listRepository.deleteById(listId);
    }

    /**
     * Normalizes a list name.
     */
    private String normalizeName(String name) {
        return name
            .trim()
            .replaceAll("\\s+", " ");
    }
}