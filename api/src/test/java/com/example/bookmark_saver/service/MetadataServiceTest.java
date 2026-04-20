package com.example.bookmark_saver.service;

import com.example.bookmark_saver.domain.Bookmark;
import com.example.bookmark_saver.domain.Metadata;
import com.example.bookmark_saver.domain.MetadataStatus;
import com.example.bookmark_saver.repository.BookmarkRepository;
import com.example.bookmark_saver.support.BookmarkFixture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MetadataServiceTest {
    /**
     * Bookmark repository mock.
     */
    @Mock
    private BookmarkRepository repository;

    /**
     * The service under test.
     * 
     * {@code Spy} allows partial mocking: we mock {@code extract} to avoid HTTP calls.
     */
    @Spy
    @InjectMocks
    private MetadataService service;

    // ---------------------------------------------------------------
    // enrich
    // ---------------------------------------------------------------

    @Test
    void enrichSetsStatusToSuccessWhenExtractionSucceeds() {
        Bookmark bookmark = BookmarkFixture.withDefaults();

        when(repository.findById(1L))
            .thenReturn(Optional.of(bookmark));

        doReturn(new Metadata())
            .when(service)
            .extract("https://example.com");

        service.enrich(1L);

        assertThat(bookmark.getMetadataStatus())
            .isEqualTo(MetadataStatus.SUCCESS);

        verify(repository).save(bookmark);
    }

    @Test
    void enrichSetsStatusToFailedWhenExtractionThrows() {
        Bookmark bookmark = BookmarkFixture.withDefaults();

        when(repository.findById(1L))
            .thenReturn(Optional.of(bookmark));

        doThrow(new RuntimeException("connection error"))
            .when(service)
            .extract("https://example.com");

        service.enrich(1L);

        assertThat(bookmark.getMetadataStatus())
            .isEqualTo(MetadataStatus.FAILED);

        verify(repository).save(bookmark);
    }

    @Test
    void enrichDoesNothingWhenBookmarkNotFound() {
        when(repository.findById(99L))
            .thenReturn(Optional.empty());

        service.enrich(99L);

        verify(repository, never()).save(any());
    }
}