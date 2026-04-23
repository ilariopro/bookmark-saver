package com.example.bookmark_saver.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.bookmark_saver.domain.Bookmark;
import com.example.bookmark_saver.domain.BookmarkList;
import com.example.bookmark_saver.domain.Tag;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@DataJpaTest
class BookmarkRepositoryIT {
    /**
     * Repository under test, injected by Spring's test context.
     */
    @Autowired
    private BookmarkRepository repository;

    /**
     * Used to persist {@link BookmarkList} entities needed by bookmark test fixtures.
     */
    @Autowired
    private BookmarkListRepository listRepository;

    /**
     * Used to persist {@link Tag} entities needed by bookmark test fixtures.
     */
    @Autowired
    private TagRepository tagRepository;

    /**
     * Creates a new {@link Bookmark} with the given URL.
     */
    private Bookmark createBookmark(String url) {
        var bookmark = new Bookmark();

        bookmark.setUrl(url);

        return bookmark;
    }

    /**
     * Creates and persists a {@link Tag} with the given name.
     *
     * @param name The name to assign to the tag.
     * @return The persisted {@link Tag} instance.
     */
    private Tag savedTag(String name) {
        var tag = new Tag();

        tag.setName(name);

        return tagRepository.save(tag);
    }

    /**
     * Creates and persists a {@link BookmarkList} with the given name.
     *
     * @param name The name to assign to the list.
     * @return The persisted {@link BookmarkList} instance.
     */
    private BookmarkList savedList(String name) {
        var list = new BookmarkList();

        list.setName(name);

        return listRepository.save(list);
    }

    // ---------------------------------------------------------------
    // CRUD
    // ---------------------------------------------------------------

    @Test
    void savePersistsEntity() {
        Bookmark bookmark = createBookmark("https://example.com");
        Bookmark saved = repository.save(bookmark);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUrl()).isEqualTo("https://example.com");
    }

    @Test
    void findByIdReturnsEntity() {
        Bookmark saved = repository.save(createBookmark("https://example.com"));

        Optional<Bookmark> result = repository.findById(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getUrl()).isEqualTo("https://example.com");
    }

    @Test
    void findByIdReturnsEmptyWhenNotFound() {
        Optional<Bookmark> result = repository.findById(999L);

        assertThat(result).isEmpty();
    }

    @Test
    void findAllReturnsAllEntities() {
        repository.save(createBookmark("https://example.com"));
        repository.save(createBookmark("https://example.org"));

        List<Bookmark> result = repository.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void deleteRemovesEntity() {
        Bookmark saved = repository.save(createBookmark("https://example.com"));

        repository.deleteById(saved.getId());

        assertThat(repository.findById(saved.getId())).isEmpty();
    }

    // ---------------------------------------------------------------
    // deleteAllByListId
    // ---------------------------------------------------------------

    @Test
    void deleteAllByListIdRemovesAssociations() {
        BookmarkList list = savedList("My List");
        Bookmark bookmark = createBookmark("https://example.com");

        bookmark.setLists(Set.of(list));
        repository.save(bookmark);

        repository.deleteAllByListId(list.getId());

        Bookmark result = repository.findById(bookmark.getId()).orElseThrow();

        assertThat(result.getLists()).isEmpty();
    }

    @Test
    void deleteAllByListIdDoesNotAffectOtherLists() {
        BookmarkList listA = savedList("List A");
        BookmarkList listB = savedList("List B");
        Bookmark bookmark = createBookmark("https://example.com");

        bookmark.setLists(Set.of(listA, listB));
        repository.save(bookmark);

        repository.deleteAllByListId(listA.getId());

        Bookmark result = repository.findById(bookmark.getId()).orElseThrow();

        assertThat(result.getLists()).containsExactly(listB);
    }

    // ---------------------------------------------------------------
    // deleteAllByTagId
    // ---------------------------------------------------------------

    @Test
    void deleteAllByTagIdRemovesAssociations() {
        Tag tag = savedTag("java");
        Bookmark bookmark = createBookmark("https://example.com");

        bookmark.setTags(Set.of(tag));
        repository.save(bookmark);

        repository.deleteAllByTagId(tag.getId());

        Bookmark result = repository.findById(bookmark.getId()).orElseThrow();

        assertThat(result.getTags()).isEmpty();
    }

    @Test
    void deleteAllByTagIdDoesNotAffectOtherTags() {
        Tag tagA = savedTag("java");
        Tag tagB = savedTag("spring");
        Bookmark bookmark = createBookmark("https://example.com");

        bookmark.setTags(Set.of(tagA, tagB));
        repository.save(bookmark);

        repository.deleteAllByTagId(tagA.getId());

        Bookmark result = repository.findById(bookmark.getId()).orElseThrow();

        assertThat(result.getTags()).containsExactly(tagB);
    }
}