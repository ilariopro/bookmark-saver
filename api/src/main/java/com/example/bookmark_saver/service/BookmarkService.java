package com.example.bookmark_saver.service;

import com.example.bookmark_saver.domain.Bookmark;
import com.example.bookmark_saver.domain.BookmarkList;
import com.example.bookmark_saver.domain.Tag;
import com.example.bookmark_saver.dto.request.BookmarkRequest;
import com.example.bookmark_saver.repository.BookmarkListRepository;
import com.example.bookmark_saver.repository.BookmarkRepository;
import com.example.bookmark_saver.repository.TagRepository;

import jakarta.persistence.EntityNotFoundException;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

/**
 * Service for managing bookmarks and their associations with lists and tags.
 *
 * Provides CRUD operations on {@link Bookmark} entities, dynamic filtering,
 * and triggers asynchronous metadata enrichment on save and URL update.
 */
@Service
public class BookmarkService {
    /**
     * Repository for accessing and persisting bookmarks.
     */
    private BookmarkRepository bookmarkRepository;

    /**
     * Repository for accessing and persisting tags.
     */
    private TagRepository tagRepository;

    /**
     * Repository for accessing and persisting lists.
     */
    private BookmarkListRepository listRepository;

    /**
     * Asynchronous bookmark metadata update service.
     */
    private MetadataService metadataService;

    /**
     * @param bookmarkRepository The bookmark repository.
     * @param tagRepository      The tag repository.
     * @param listRepository     The list repository.
     * @param metadataService    The metadata enrichment service.
     */
    public BookmarkService(
        BookmarkRepository bookmarkRepository,
        TagRepository tagRepository,
        BookmarkListRepository listRepository,
        MetadataService metadataService
    ) {
        this.bookmarkRepository = bookmarkRepository;
        this.tagRepository = tagRepository;
        this.listRepository = listRepository;
        this.metadataService = metadataService;
    }

    /**
     * Returns a paginated list of bookmarks, optionally filtered.
     *
     * @param favorite If non-null, filters by favorite status.
     * @param listIds  If non-blank, filters by list ids.
     * @param tagIds   If non-blank, filters by tag ids.
     * @param pageable Pagination options.
     * 
     * @return A {@link Page} of matching {@link Bookmark} entities.
     */
    public Page<Bookmark> findAll(
        Boolean favorite,
        List<Long> listIds,
        List<Long> tagIds,
        Pageable pageable
    ) {
        Specification<Bookmark> spec = (root, query, criteria) -> criteria.conjunction();

        if (favorite != null) {
            spec = spec.and((root, query, criteria) ->
                criteria.equal(
                    root.get("favorite"),
                    favorite
                )
            );
        }

        if (listIds != null && !listIds.isEmpty()) {
            for (Long listId : listIds) {
                spec = spec.and((root, query, criteria) -> {
                    query.distinct(true);

                    return criteria.equal(
                        root.join("lists").get("id"),
                        listId
                    );
                });
            }
        }

        if (tagIds != null && !tagIds.isEmpty()) {
            for (Long tagId : tagIds) {
                spec = spec.and((root, query, criteria) -> {
                    query.distinct(true);

                    return criteria.equal(
                        root.join("tags").get("id"),
                        tagId
                    );
                });
            }
        }

        return bookmarkRepository.findAll(spec, pageable);
    }

    /**
     * Finds a bookmark by its ID.
     *
     * @param bookmarkId The ID of the bookmark.
     * 
     * @return The matching {@link Bookmark}.
     * @throws EntityNotFoundException If no bookmark is found with the given ID.
     */
    public Bookmark findById(Long bookmarkId) {
        return bookmarkRepository
                .findById(bookmarkId)
                .orElseThrow(
                    () -> new EntityNotFoundException( "Bookmark not found with id: " + bookmarkId)
                );
    }

    /**
     * Creates a new bookmark, then triggers asynchronous metadata enrichment.
     *
     * @param request The bookmark creation request.
     * 
     * @return The saved {@link Bookmark}.
     * @throws EntityNotFoundException If any of the provided tag IDs do not exist.
     */
    public Bookmark save(BookmarkRequest request) {
        Bookmark bookmark = new Bookmark();
        
        bookmark.setUrl(request.url());
        bookmark.setNotes(request.notes());
        bookmark.setFavorite(request.favorite());
        bookmark.setLists(fetchLists(request.listIds()));
        bookmark.setTags(fetchTags(request.tagIds()));
        
        Bookmark saved = bookmarkRepository.save(bookmark);

        metadataService.enrich(saved.getId());

        return saved;
    }

    /**
     * Updates an existing bookmark.
     * Triggers metadata enrichment only if the URL has changed.
     *
     * @param bookmarkId The ID of the bookmark to update.
     * @param request    The update request.
     * 
     * @return The updated {@link Bookmark}.
     * @throws EntityNotFoundException If the bookmark or tag ID is not found.
     */
    public Bookmark update(
        Long bookmarkId,
        BookmarkRequest request
    ) {
        Bookmark bookmark = findById(bookmarkId);

        String previousUrl = bookmark.getUrl();
        String currentUrl  = request.url() != null ? request.url() : previousUrl;

        bookmark.setUrl(currentUrl);
        bookmark.setNotes(request.notes());
        bookmark.setFavorite(request.favorite());
        bookmark.setLists(fetchLists(request.listIds()));
        bookmark.setTags(fetchTags(request.tagIds()));

        Bookmark updated = bookmarkRepository.save(bookmark);

        if (!previousUrl.equals(currentUrl)) {
            metadataService.enrich(updated.getId());
        }

        return updated;
    }

    /**
     * Replaces the list associations of a bookmark.
     *
     * @param bookmarkId The ID of the bookmark.
     * @param listIds    The IDs of the lists to associate.
     * 
     * @return The updated {@link Bookmark}.
     * @throws EntityNotFoundException If the bookmark or any list ID is not found.
     */
    public Bookmark updateLists(Long bookmarkId, List<Long> listIds) {
        Bookmark bookmark = findById(bookmarkId);

        bookmark.setLists(fetchLists(listIds));

        return bookmarkRepository.save(bookmark);
    }

    /**
     * Replaces the tag associations of a bookmark.
     *
     * @param bookmarkId The ID of the bookmark.
     * @param tagIds     The IDs of the tags to associate.
     * 
     * @return The updated {@link Bookmark}.
     * @throws EntityNotFoundException If the bookmark or any tag ID is not found.
     */
    public Bookmark updateTags(Long bookmarkId, List<Long> tagIds) {
        Bookmark bookmark = findById(bookmarkId);

        bookmark.setTags(fetchTags(tagIds));

        return bookmarkRepository.save(bookmark);
    }

    /**
     * Deletes a bookmark by its ID.
     *
     * @param bookmarkId The ID of the bookmark to delete.
     * 
     * @throws EntityNotFoundException If no bookmark is found with the given ID.
     */
    public void delete(Long bookmarkId) {
        Bookmark bookmark = findById(bookmarkId);

        bookmarkRepository.delete(bookmark);
    }

    /**
     * Fetches and validates a set of {@link BookmarkList} entities by their IDs.
     */
    private Set<BookmarkList> fetchLists(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }

        List<BookmarkList> lists = listRepository.findAllById(ids);

        if (lists.size() != ids.size()) {
            throw new EntityNotFoundException(
                "One or more lists not found. Expected all listIds to exist: " + ids
            );
        }

        return new HashSet<>(lists);
    }

    /**
     * Fetches and validates a set of {@link Tag} entities by their IDs.
     */
    private Set<Tag> fetchTags(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }

        List<Tag> tags = tagRepository.findAllById(ids);

        if (tags.size() != ids.size()) {
            throw new EntityNotFoundException(
                "One or more tags not found. Expected all tagIds to exist: " + ids
            );
        }

        return new HashSet<>(tags);
    }
}