package com.example.bookmark_saver.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.example.bookmark_saver.domain.Tag;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.util.List;
import java.util.Optional;

@DataJpaTest
class TagRepositoryIT {
    /**
     * Repository under test, injected by Spring's test context.
     */
    @Autowired
    private TagRepository repository;

    /**
     * Creates a new {@link Tag} with the given name.
     */
    private Tag createTag(String name) {
        var tag = new Tag();

        tag.setName(name);

        return tag;
    }

    @Test
    void savePersistsEntity() {
        Tag tag = createTag("java");
        Tag saved = repository.save(tag);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("java");
    }

    @Test
    void findByIdReturnsEntity() {
        Tag saved = repository.save(createTag("java"));

        Optional<Tag> result = repository.findById(saved.getId());

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("java");
    }

    @Test
    void findAllReturnsAllEntities() {
        repository.save(createTag("java"));
        repository.save(createTag("spring"));

        List<Tag> result = repository.findAll();

        assertThat(result).hasSize(2);
    }

    @Test
    void findByNameReturnsEntity() {
        repository.save(createTag("java"));

        Optional<Tag> result = repository.findByName("java");

        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("java");
    }

    @Test
    void findByNameReturnsEmptyWhenNotFound() {
        Optional<Tag> result = repository.findByName("nonexistent");

        assertThat(result).isEmpty();
    }

    @Test
    void deleteRemovesEntity() {
        Tag saved = repository.save(createTag("java"));

        repository.deleteById(saved.getId());

        assertThat(repository.findById(saved.getId())).isEmpty();
    }
}