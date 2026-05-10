package com.example.bookmark_saver.service;

import com.example.bookmark_saver.domain.BookmarkList;
import com.example.bookmark_saver.dto.request.BookmarkListRequest;
import com.example.bookmark_saver.repository.BookmarkListRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.data.domain.Sort;
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
     * @param listRepository      The list repository.
     */
    public BookmarkListService(
        BookmarkListRepository listRepository
    ) {
        this.listRepository = listRepository;
    }

    /**
     * Returns all lists.
     * 
     * @param sort Sort options
     * 
     * @return The complete list of {@link BookmarkList}
     */
    public List<BookmarkList> findAll(Sort sort) {
        return listRepository.findAll(sort);
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
        
        String name = request.name() != null ? normalizeName(request.name()) : list.getName();
        String description = request.description() != null ? request.description() : list.getDescription();

        list.setName(name);
        list.setDescription(description);

        return listRepository.save(list);
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