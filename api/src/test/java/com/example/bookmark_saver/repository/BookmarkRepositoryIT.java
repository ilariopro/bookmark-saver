package com.example.bookmark_saver.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.bookmark_saver.domain.Bookmark;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

@DataJpaTest
class BookmarkRepositoryIT {
    /**
     * Repository under test, injected by Spring's test context.
     */
    @Autowired
    private BookmarkRepository repository;

    /**
     * Creates a new {@link Bookmark} with the given URL.
     */
    private Bookmark createBookmark(String url) {
        var bookmark = new Bookmark();

        bookmark.setUrl(url);

        return bookmark;
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
}