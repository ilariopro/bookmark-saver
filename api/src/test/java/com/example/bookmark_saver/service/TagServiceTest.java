package com.example.bookmark_saver.service;

import com.example.bookmark_saver.domain.Tag;
import com.example.bookmark_saver.repository.BookmarkRepository;
import com.example.bookmark_saver.repository.TagRepository;
import com.example.bookmark_saver.support.TagFixture;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TagServiceTest {
    /**
     * Tag repository mock.
     */
    @Mock
    private TagRepository tagRepository;

    /**
     * Bookmark repository mock.
     */
    @Mock
    private BookmarkRepository bookmarkRepository;

    /**
     * JDBC template mock.
     */
    @Mock
    private JdbcTemplate jdbcTemplate;

    /**
     * The service under test, with mocks injected automatically.
     */
    @InjectMocks
    private TagService service;

    // ---------------------------------------------------------------
    // findAll
    // ---------------------------------------------------------------

    @Test // FIXME update this method to reflect new features
    void findAllReturnsAllTags() {
        List<Tag> tags = List.of(TagFixture.withDefaults());

        when(tagRepository.findAll(any(Sort.class)))
            .thenReturn(tags);

        List<Tag> result = service.findAll(Sort.unsorted());

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("Java");
    }

    // ---------------------------------------------------------------
    // findById
    // ---------------------------------------------------------------

    @Test
    void findByIdReturnsTag() {
        when(tagRepository.findById(1L))
            .thenReturn(Optional.of(TagFixture.withDefaults()));

        Tag result = service.findById(1L);

        assertThat(result.getName()).isEqualTo("Java");
    }

    @Test
    void findByIdThrowsWhenNotFound() {
        when(tagRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    // ---------------------------------------------------------------
    // save
    // ---------------------------------------------------------------

    // TODO add tests

    // ---------------------------------------------------------------
    // update
    // ---------------------------------------------------------------

    // TODO add tests

    // ---------------------------------------------------------------
    // delete
    // ---------------------------------------------------------------

    @Test
    void deleteRemovesTag() {
        when(tagRepository.findById(1L))
            .thenReturn(Optional.of(TagFixture.withDefaults()));

        service.delete(1L);

        verify(tagRepository).deleteById(1L);
    }

    @Test
    void deleteThrowsWhenTagNotFound() {
        when(tagRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }
}