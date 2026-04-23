package com.example.bookmark_saver.service;

import com.example.bookmark_saver.domain.Bookmark;
import com.example.bookmark_saver.domain.BookmarkList;
import com.example.bookmark_saver.domain.Tag;
import com.example.bookmark_saver.dto.request.BookmarkRequest;
import com.example.bookmark_saver.repository.BookmarkListRepository;
import com.example.bookmark_saver.repository.BookmarkRepository;
import com.example.bookmark_saver.repository.TagRepository;
import com.example.bookmark_saver.support.BookmarkFixture;
import com.example.bookmark_saver.support.BookmarkListFixture;
import com.example.bookmark_saver.support.TagFixture;

import jakarta.persistence.EntityNotFoundException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookmarkServiceTest {
    /**
     * Bookmark repository mock.
     */
    @Mock
    private BookmarkRepository bookmarkRepository;

    /**
     * List repository mock.
     */
    @Mock
    private BookmarkListRepository listRepository;

    /**
     * Tag repository mock.
     */
    @Mock
    private TagRepository tagRepository;

    /**
     * Metadata enrichment service mock.
     */
    @Mock
    private MetadataService metadataService;

    /**
     * The service under test, with mocks injected automatically.
     */
    @InjectMocks
    private BookmarkService service;

    /**
     * Matches any {@link Specification}{@code <Bookmark>} in Mockito stubs.
     * 
     * @return The typed {@link Specification}.
     */
    private Specification<Bookmark> anySpec() {
        return any();
    }

    // ---------------------------------------------------------------
    // findAll
    // ---------------------------------------------------------------

    @Test
    void findAllReturnsPaginatedBookmarks() {
        Page<Bookmark> page = new PageImpl<>(List.of(BookmarkFixture.withDefaults()));

        when(bookmarkRepository.findAll(anySpec(), any(Pageable.class)))
            .thenReturn(page);

        Page<Bookmark> result = service.findAll(null, null, List.of(), Pageable.unpaged());

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getUrl()).isEqualTo("https://example.com");
    }

    @Test
    void findAllWithFavoriteFilterPassesSpecificationToRepository() {
        when(bookmarkRepository.findAll(anySpec(), any(Pageable.class)))
            .thenReturn(Page.empty());

        service.findAll(true, null, List.of(), Pageable.unpaged());

        verify(bookmarkRepository).findAll(anySpec(), any(Pageable.class));
    }

    // ---------------------------------------------------------------
    // findById
    // ---------------------------------------------------------------

    @Test
    void findByIdReturnsBookmark() {
        when(bookmarkRepository.findById(1L))
            .thenReturn(Optional.of(BookmarkFixture.withDefaults()));

        Bookmark result = service.findById(1L);

        assertThat(result.getUrl()).isEqualTo("https://example.com");
    }

    @Test
    void findByIdThrowsWhenNotFound() {
        when(bookmarkRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    // ---------------------------------------------------------------
    // save
    // ---------------------------------------------------------------

    @Test
    void savePersistsBookmarkAndTriggersMetadataEnrichment() {
        Bookmark saved = BookmarkFixture.withDefaults();
        
        BookmarkRequest request = new BookmarkRequest(
            "https://example.com",
            "notes",
            false,
            List.of(),
            List.of()
        );

        when(bookmarkRepository.save(any()))
            .thenReturn(saved);

        Bookmark result = service.save(request);

        assertThat(result.getUrl()).isEqualTo("https://example.com");
        verify(metadataService).enrich(1L);
    }

    @Test
    void saveThrowsWhenListIdDoesNotExist() {
        BookmarkRequest request = new BookmarkRequest(
            "https://example.com",
            "notes",
            false,
            List.of(99L),
            List.of()
        );

        when(listRepository.findAllById(List.of(99L)))
            .thenReturn(List.of());

        assertThatThrownBy(() -> service.save(request))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    @Test
    void saveThrowsWhenTagIdDoesNotExist() {
        BookmarkRequest request = new BookmarkRequest(
            "https://example.com",
            "notes",
            false,
            List.of(),
            List.of(99L)
        );

        when(tagRepository.findAllById(List.of(99L)))
            .thenReturn(List.of());

        assertThatThrownBy(() -> service.save(request))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    // ---------------------------------------------------------------
    // update
    // ---------------------------------------------------------------

    @Test
    void updatePersistsChanges() {
        Bookmark existing = BookmarkFixture.withUrl("https://old.com");

        BookmarkRequest request = new BookmarkRequest(
            "https://old.com",
            "new notes",
            true,
            List.of(),
            List.of()
        );

        when(bookmarkRepository.findById(1L))
            .thenReturn(Optional.of(existing));

        when(bookmarkRepository.save(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Bookmark result = service.update(1L, request);

        assertThat(result.getNotes()).isEqualTo("new notes");
        assertThat(result.isFavorite()).isTrue();
    }

    @Test
    void updateTriggersMetadataEnrichmentWhenUrlChanges() {
        Bookmark existing = BookmarkFixture.create(1L, "https://old.com", "Old notes");
        Bookmark updated = BookmarkFixture.create(1L, "https://new.com", "New notes");
        
        BookmarkRequest request = new BookmarkRequest(
            "https://new.com",
            "New notes",
            false,
            List.of(),
            List.of()
        );

        when(bookmarkRepository.findById(1L))
            .thenReturn(Optional.of(existing));

        when(bookmarkRepository.save(any()))
            .thenReturn(updated);

        service.update(1L, request);

        verify(metadataService).enrich(1L);
    }

    @Test
    void updateDoesNotTriggerMetadataEnrichmentWhenUrlIsUnchanged() {
        Bookmark existing = BookmarkFixture.withDefaults();
        
        BookmarkRequest request = new BookmarkRequest(
            "https://example.com",
            "new notes",
            false,
            List.of(),
            List.of()
        );

        when(bookmarkRepository.findById(1L))
            .thenReturn(Optional.of(existing));

        when(bookmarkRepository.save(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        service.update(1L, request);

        verify(metadataService, never()).enrich(anyLong());
    }

    @Test
    void updateThrowsWhenBookmarkNotFound() {
        BookmarkRequest request = new BookmarkRequest(
            "https://example.com",
            "",
            false,
            List.of(),
            List.of()
        );

        when(bookmarkRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(99L, request))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    // ---------------------------------------------------------------
    // updateLists
    // ---------------------------------------------------------------

    @Test
    void updateListsReplacesAllAssociations() {
        Bookmark existing = BookmarkFixture.withDefaults();
        BookmarkList list = BookmarkListFixture.withId(10L);

        when(bookmarkRepository.findById(1L))
            .thenReturn(Optional.of(existing));

        when(listRepository.findAllById(List.of(10L)))
            .thenReturn(List.of(list));
        
        when(bookmarkRepository.save(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Bookmark result = service.updateLists(1L, List.of(10L));

        assertThat(result.getLists()).containsExactly(list);
    }

    @Test
    void updateListsThrowsWhenBookmarkNotFound() {
        when(bookmarkRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateLists(99L, List.of(1L)))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    // ---------------------------------------------------------------
    // updateTags
    // ---------------------------------------------------------------

    @Test
    void updateTagsReplacesAllAssociations() {
        Bookmark existing = BookmarkFixture.withDefaults();
        Tag tag = TagFixture.withId(10L);

        when(bookmarkRepository.findById(1L))
            .thenReturn(Optional.of(existing));

        when(tagRepository.findAllById(List.of(10L)))
            .thenReturn(List.of(tag));
        
        when(bookmarkRepository.save(any()))
            .thenAnswer(invocation -> invocation.getArgument(0));

        Bookmark result = service.updateTags(1L, List.of(10L));

        assertThat(result.getTags()).containsExactly(tag);
    }

    @Test
    void updateTagsThrowsWhenBookmarkNotFound() {
        when(bookmarkRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.updateTags(99L, List.of(1L)))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }

    // ---------------------------------------------------------------
    // delete
    // ---------------------------------------------------------------

    @Test
    void deleteRemovesBookmark() {
        Bookmark existing = BookmarkFixture.withDefaults();

        when(bookmarkRepository.findById(1L))
            .thenReturn(Optional.of(existing));

        service.delete(1L);

        verify(bookmarkRepository).delete(existing);
    }

    @Test
    void deleteThrowsWhenBookmarkNotFound() {
        when(bookmarkRepository.findById(99L))
            .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(99L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("99");
    }
}