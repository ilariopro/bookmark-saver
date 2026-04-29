package com.example.bookmark_saver.controller;

import com.example.bookmark_saver.domain.BookmarkList;
import com.example.bookmark_saver.dto.request.BookmarkListRequest;
import com.example.bookmark_saver.service.BookmarkListService;
import com.example.bookmark_saver.support.BookmarkListFixture;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.security.oauth2.server.resource.autoconfigure.servlet.OAuth2ResourceServerAutoConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
    controllers = BookmarkListController.class,
    excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class
)
class BookmarkListControllerTest {
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
    private BookmarkListService service;

    // ---------------------------------------------------------------
    // GET /api/lists
    // ---------------------------------------------------------------

    @Test
    void listReturnsAllLists() throws Exception {
        List<BookmarkList> lists = List.of(BookmarkListFixture.withDefaults());

        when(service.findAll(any()))
            .thenReturn(lists);

        mockMvc
            .perform(get("/api/lists"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].name").value("My List"));
    }

    // ---------------------------------------------------------------
    // GET /api/lists/{listId}
    // ---------------------------------------------------------------

    @Test
    void getExistingIdReturnsList() throws Exception {
        when(service.findById(1L))
            .thenReturn(BookmarkListFixture.withDefaults());

        mockMvc
            .perform(get("/api/lists/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("My List"));
    }

    // ---------------------------------------------------------------
    // POST /api/lists
    // ---------------------------------------------------------------

    @Test
    void createValidRequestReturns201() throws Exception {
        BookmarkListRequest request = new BookmarkListRequest("My List", null);
        BookmarkList saved = BookmarkListFixture.withDefaults();

        when(service.save(any()))
            .thenReturn(saved);

        mockMvc
            .perform(post("/api/lists")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.name").value("My List"))
            .andExpect(jsonPath("$.data.description").isEmpty());

        verify(service).save(any());
    }

    @Test
    void createInvalidNameReturns400() throws Exception {
        String invalidBody = """
            { "name": "" }
        """;

        mockMvc
            .perform(post("/api/lists")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidBody))
            .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------
    // PUT /api/lists/{listId}
    // ---------------------------------------------------------------

    @Test
    void updateExistingIdReturnsUpdatedList() throws Exception {
        BookmarkListRequest request = new BookmarkListRequest("Updated List", null);
        BookmarkList updated = BookmarkListFixture.withName("Updated List");

        when(service.update(eq(1L), any()))
            .thenReturn(updated);

        mockMvc
            .perform(patch("/api/lists/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("Updated List"));

        verify(service).update(eq(1L), any());
    }

    // ---------------------------------------------------------------
    // DELETE /api/lists/{listId}
    // ---------------------------------------------------------------

    @Test
    void deleteExistingIdReturns204() throws Exception {
        doNothing().when(service).delete(1L);

        mockMvc
            .perform(delete("/api/lists/1"))
            .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }
}