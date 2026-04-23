package com.example.bookmark_saver.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.bookmark_saver.domain.BookmarkList;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

@DataJpaTest
class BookmarkListRepositoryTest {
    /**
     * Repository under test, injected by Spring's test context.
     */
    @Autowired
    private BookmarkListRepository repository;

    /**
     * Creates a new {@link BookmarkList} with the given name.
     */
    private BookmarkList createList(String name) {
        var list = new BookmarkList();

        list.setName(name);

        return list;
    }

    @Test
    void savePersistsEntity() {
        BookmarkList list = createList("My List");
        BookmarkList saved = repository.save(list);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("My List");
    }

    @Test
    void findByIdReturnsEntity() {
        BookmarkList list = createList("My List");
        BookmarkList saved = repository.save(list);

        Optional<BookmarkList> result = repository.findById(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("My List");
    }

    @Test
    void findAllReturnsAllEntities() {
        repository.save(createList("List 1"));
        repository.save(createList("List 2"));

        List<BookmarkList> result = repository.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void deleteRemovesEntity() {
        BookmarkList list = repository.save(createList("Delete me"));

        repository.deleteById(list.getId());

        assertThat(repository.findById(list.getId())).isEmpty();
    }
}