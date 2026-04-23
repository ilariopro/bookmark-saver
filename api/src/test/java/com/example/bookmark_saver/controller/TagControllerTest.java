package com.example.bookmark_saver.controller;

import com.example.bookmark_saver.domain.Tag;
import com.example.bookmark_saver.dto.request.TagRequest;
import com.example.bookmark_saver.service.TagService;
import com.example.bookmark_saver.support.TagFixture;
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
    controllers = TagController.class,
    excludeAutoConfiguration = OAuth2ResourceServerAutoConfiguration.class
)
class TagControllerTest {
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
    private TagService service;

    // ---------------------------------------------------------------
    // GET /api/tags
    // ---------------------------------------------------------------

    @Test
    void listReturnsTagList() throws Exception {
        List<Tag> tags = List.of(TagFixture.withDefaults());

        when(service.findAll())
            .thenReturn(tags);

        mockMvc
            .perform(get("/api/tags"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data[0].name").value("java"));
    }

    // ---------------------------------------------------------------
    // GET /api/tags/{tagId}
    // ---------------------------------------------------------------

    @Test
    void getExistingIdReturnsTag() throws Exception {
        when(service.findById(1L))
            .thenReturn(TagFixture.withDefaults());

        mockMvc
            .perform(get("/api/tags/1"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("java"));
    }

    // ---------------------------------------------------------------
    // POST /api/tags
    // ---------------------------------------------------------------

    @Test
    void createValidRequestReturns201() throws Exception {
        TagRequest request = new TagRequest("java");
        Tag saved = TagFixture.withDefaults();

        when(service.save(any()))
            .thenReturn(saved);

        mockMvc
            .perform(post("/api/tags")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.data.name").value("java"));
    }

    @Test
    void createInvalidNameReturns400() throws Exception {
        String invalidBody = """
            { "name": "" }
        """;

        mockMvc
            .perform(post("/api/tags")
            .contentType(MediaType.APPLICATION_JSON)
            .content(invalidBody))
            .andExpect(status().isBadRequest());
    }

    // ---------------------------------------------------------------
    // PUT /api/tags/{tagId}
    // ---------------------------------------------------------------

    @Test
    void updateExistingIdReturnsUpdatedTag() throws Exception {
        TagRequest request = new TagRequest("spring");
        Tag updated = TagFixture.withName("spring");

        when(service.update(eq(1L), any()))
            .thenReturn(updated);

        mockMvc
            .perform(put("/api/tags/1")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.data.name").value("spring"));
    }

    // ---------------------------------------------------------------
    // DELETE /api/tags/{tagId}
    // ---------------------------------------------------------------

    @Test
    void deleteExistingIdReturns204() throws Exception {
        doNothing().when(service).delete(1L);

        mockMvc
            .perform(delete("/api/tags/1"))
            .andExpect(status().isNoContent());

        verify(service).delete(1L);
    }
}