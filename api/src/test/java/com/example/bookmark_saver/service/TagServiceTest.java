package com.example.bookmark_saver.service;

import com.example.bookmark_saver.domain.Tag;
import com.example.bookmark_saver.dto.request.TagRequest;
import com.example.bookmark_saver.repository.BookmarkRepository;
import com.example.bookmark_saver.repository.TagRepository;
import com.example.bookmark_saver.support.TagFixture;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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

    @Test
    void findAllReturnsAllTags() {
        List<Tag> tags = List.of(TagFixture.withDefaults());

        when(tagRepository.findAll())
            .thenReturn(tags);

        List<Tag> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("java");
    }

    // ---------------------------------------------------------------
    // findById
    // ---------------------------------------------------------------

    @Test
    void findByIdReturnsTag() {
        when(tagRepository.findById(1L))
            .thenReturn(Optional.of(TagFixture.withDefaults()));

        Tag result = service.findById(1L);

        assertThat(result.getName()).isEqualTo("java");
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

    @Test
    void saveNormalizesNameBeforePersisting() {
        when(tagRepository.save(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Tag result = service.save(new TagRequest("  Java  "));

        assertThat(result.getName()).isEqualTo("java");
    }

    // ---------------------------------------------------------------
    // update
    // ---------------------------------------------------------------

    @Test
    void updateRenamesTagWhenNameIsUnique() {
        Tag existing = TagFixture.withDefaults();

        when(tagRepository.findById(1L))
            .thenReturn(Optional.of(existing));

        when(tagRepository.findByName("spring"))
            .thenReturn(Optional.empty());

        when(tagRepository.save(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));


        Tag result = service.update(1L, new TagRequest("spring"));

        assertThat(result.getName()).isEqualTo("spring");
    }

    @Test
    void updateMergesTagsWhenNameAlreadyExists() {
        Tag tagToUpdate = TagFixture.create(1L, "java");
        Tag conflictingTag = TagFixture.create(2L, "spring");

        when(tagRepository.findById(1L))
            .thenReturn(Optional.of(tagToUpdate));

        when(tagRepository.findByName("spring"))
            .thenReturn(Optional.of(conflictingTag));

        service.update(1L, new TagRequest("spring"));

        // The conflicting tag must be deleted and relationships must be moved
        verify(jdbcTemplate, times(2)).update(anyString(), any(Object[].class));
        verify(tagRepository).deleteById(2L);
    }

    @Test
    void updateDoesNotMergeWhenNameBelongsToSameTag() {
        Tag tag = TagFixture.withDefaults();

        when(tagRepository.findById(1L))
            .thenReturn(Optional.of(tag));

        when(tagRepository.findByName("java"))
            .thenReturn(Optional.of(tag));

        when(tagRepository.save(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));


        Tag result = service.update(1L, new TagRequest("java"));

        assertThat(result.getName()).isEqualTo("java");
        verify(tagRepository, never()).deleteById(any());
    }

    // ---------------------------------------------------------------
    // updateBookmarks
    // ---------------------------------------------------------------

    @Test
    void updateBookmarksReplacesAllAssociations() {
        when(tagRepository.findById(1L))
            .thenReturn(Optional.of(TagFixture.withDefaults()));

        service.updateBookmarks(1L, List.of(10L, 20L));

        verify(bookmarkRepository).deleteAllByTagId(1L);
        verify(jdbcTemplate).batchUpdate(anyString(), anyList(), anyInt(), any());
    }

    @Test
    void updateBookmarksThrowsWhenTagNotFound() {
        when(tagRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateBookmarks(99L, List.of(1L)))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

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