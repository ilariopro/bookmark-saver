package com.example.bookmark_saver.service;

import com.example.bookmark_saver.domain.BookmarkList;
import com.example.bookmark_saver.dto.request.BookmarkListRequest;
import com.example.bookmark_saver.repository.BookmarkRepository;
import com.example.bookmark_saver.repository.BookmarkListRepository;
import com.example.bookmark_saver.support.BookmarkListFixture;

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
class BookmarkListServiceTest {
    /**
     * List repository mock.
     */
    @Mock
    private BookmarkListRepository listRepository;

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
    private BookmarkListService service;

    // ---------------------------------------------------------------
    // findAll
    // ---------------------------------------------------------------

    @Test
    void findAllReturnsAllLists() {
        List<BookmarkList> lists = List.of(BookmarkListFixture.withDefaults());

        when(listRepository.findAll())
            .thenReturn(lists);

        List<BookmarkList> result = service.findAll();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("My List");
    }

    // ---------------------------------------------------------------
    // findById
    // ---------------------------------------------------------------

    @Test
    void findByIdReturnsList() {
        when(listRepository.findById(1L))
            .thenReturn(Optional.of(BookmarkListFixture.withDefaults()));

        BookmarkList result = service.findById(1L);

        assertThat(result.getName()).isEqualTo("My List");
    }

    @Test
    void findByIdThrowsWhenNotFound() {
        when(listRepository.findById(99L))
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
        when(listRepository.save(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        BookmarkList result = service.save(new BookmarkListRequest("  My list  ", null));

        assertThat(result.getName()).isEqualTo("My list");
    }

    // ---------------------------------------------------------------
    // update
    // ---------------------------------------------------------------

    @Test
    void updateRenamesListWhenNameIsUnique() {
        BookmarkList existing = BookmarkListFixture.withDefaults();

        when(listRepository.findById(1L))
            .thenReturn(Optional.of(existing));

        when(listRepository.save(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));


        BookmarkList result = service.update(1L, new BookmarkListRequest("Updated List", null));

        assertThat(result.getName()).isEqualTo("Updated List");
    }

    // ---------------------------------------------------------------
    // updateBookmarks
    // ---------------------------------------------------------------

    @Test
    void updateBookmarksReplacesAllAssociations() {
        when(listRepository.findById(1L))
            .thenReturn(Optional.of(BookmarkListFixture.withDefaults()));

        service.updateBookmarks(1L, List.of(10L, 20L));

        verify(bookmarkRepository).deleteAllByListId(1L);
        verify(jdbcTemplate).batchUpdate(anyString(), anyList(), anyInt(), any());
    }

    @Test
    void updateBookmarksThrowsWhenListNotFound() {
        when(listRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateBookmarks(99L, List.of(1L)))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    // ---------------------------------------------------------------
    // delete
    // ---------------------------------------------------------------

    @Test
    void deleteRemovesList() {
        when(listRepository.findById(1L))
            .thenReturn(Optional.of(BookmarkListFixture.withDefaults()));

        service.delete(1L);

        verify(listRepository).deleteById(1L);
    }

    @Test
    void deleteThrowsWhenListNotFound() {
        when(listRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }
}