package com.example.bookmark_saver.controller;

import com.example.bookmark_saver.domain.Bookmark;
import com.example.bookmark_saver.dto.request.BookmarkRequest;
import com.example.bookmark_saver.service.BookmarkService;
import com.example.bookmark_saver.support.BookmarkFixture;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = BookmarkController.class,
    excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class
)
class BookmarkControllerTest {
    /**
     * HTTP test client.
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * Serialize/deserialize JSON.
     */
    private ObjectMapper objectMapper = new ObjectMapper();

    /**
     * Service mock.
     */
    @MockitoBean
    private BookmarkService service;

    // ---------------------------------------------------------------
    // GET /api/bookmarks
    // ---------------------------------------------------------------

    @Test
    void listReturnsPagedBookmarks() throws Exception {
        Bookmark bookmark = BookmarkFixture.withDefaults();
        Page<Bookmark> page = new PageImpl<>(List.of(bookmark));

        when(service.findAll(any(), any(), any(), any()))
            .thenReturn(page);

        mockMvc
            .perform(get("/api/bookmarks"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].url")
            .value("https://example.com"));
    }

    @Test
    void listWithFavoriteFilterPassesParamToService() throws Exception {
        when(service.findAll(eq(true), any(), any(), any()))
            .thenReturn(Page.empty());

        mockMvc
            .perform(get("/api/bookmarks")
            .param("favorite", "true"))
            .andExpect(status().isOk());

        verify(service).findAll(eq(true), any(), any(), any());
    }

    @Test
    void listWithListIdsParsesAndPassesIds() throws Exception {
        when(service.findAll(any(), eq(List.of(1L, 2L)), any(), any()))
            .thenReturn(Page.empty());

        mockMvc
            .perform(get("/api/bookmarks")
            .param("listIds", "1,2"))
            .andExpect(status().isOk());

        verify(service).findAll(any(), eq(List.of(1L, 2L)), any(), any());
    }

    @Test
    void listWithTagIdsParsesAndPassesIds() throws Exception {
        when(service.findAll(any(), any(), eq(List.of(1L, 2L)), any()))
            .thenReturn(Page.empty());

        mockMvc
            .perform(get("/api/bookmarks")
            .param("tagIds", "1,2"))
            .andExpect(status().isOk());

        verify(service).findAll(any(), any(), eq(List.of(1L, 2L)), any());
    }

    // ---------------------------------------------------------------
    // GET /api/bookmarks/{bookmarkId}
    // ---------------------------------------------------------------

    @Test
    void getExistingIdReturnsBookmark() throws Exception {
        when(service.findById(1L))
            .thenReturn(BookmarkFixture.withDefaults());

        mockMvc
            .perform(get("/api/bookmarks/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.url").value("https://example.com"));
    }

    // ---------------------------------------------------------------
    // POST /api/bookmarks
    // ---------------------------------------------------------------

    @Test
    void createValidRequestReturns201() throws Exception {
        Bookmark saved = BookmarkFixture.withDefaults();
        
        BookmarkRequest request = new BookmarkRequest(
            "https://example.com",
            "notes",
            false,
            List.of(),
            List.of()
        );

        when(service.save(any()))
            .thenReturn(saved);

        mockMvc
            .perform(post("/api/bookmarks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.url").value("https://example.com"));

        verify(service).save(any());
    }

    @Test
    void createInvalidUrlReturns400() throws Exception {
        String invalidBody = """
            { "url": "" }
        """;

        mockMvc
            .perform(post("/api/bookmarks")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidBody))
            .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------
    // PUT /api/bookmarks/{bookmarkId}
    // ---------------------------------------------------------------

    @Test
    void updateExistingIdReturnsUpdatedBookmark() throws Exception {
        Bookmark updated = BookmarkFixture.withUrl("https://updated.com");
        
        BookmarkRequest request = new BookmarkRequest(
            "https://updated.com",
            "notes",
            true,
            List.of(),
            List.of()
        );

        when(service.update(eq(1L), any()))
            .thenReturn(updated);

        mockMvc
            .perform(put("/api/bookmarks/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.url").value("https://updated.com"));

        verify(service).update(eq(1L), any());
    }

    // ---------------------------------------------------------------
    // DELETE /api/bookmarks/{bookmarkId}
    // ---------------------------------------------------------------

    @Test
    void deleteExistingIdReturns204() throws Exception {
        doNothing().when(service).delete(1L);

        mockMvc
            .perform(delete("/api/bookmarks/1"))
            .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }
}