package com.example.bookmark_saver.utility;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.function.Function;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import com.example.bookmark_saver.dto.common.ApiListResponse;
import com.example.bookmark_saver.dto.common.ApiResponse;

class ResponseFactoryTest {
    /**
     * Provides a simple mapping function used in tests.
     *
     * Example: 1 -> "v1"
     *
     * @return A function mapping an integer to a prefixed string.
     */
    private Function<Integer, String> mapper() {
        return value -> "v" + value;
    }

    @Test
    void oneMapsSingleElement() {
        ApiResponse<String> response = ResponseFactory.one(1, mapper());

        assertThat(response.data()).isEqualTo("v1");
    }

    @Test
    void listMapsElementsWithoutPageInfo() {
        ApiListResponse<String> response = ResponseFactory.list(
            List.of(1, 2),
            mapper()
        );

        assertThat(response.data()).containsExactly("v1", "v2");
        assertThat(response.meta()).isNull();
    }

    @Test
    void listReturnsEmptyListWhenInputEmpty() {
        ApiListResponse<String> response = ResponseFactory.list(
            List.of(),
            mapper()
        );

        assertThat(response.data()).isEmpty();
        assertThat(response.meta()).isNull();
    }

    @Test
    void pageMapsElementsAndIncludesPageInfo() {
        Page<Integer> page = new PageImpl<>(
            List.of(1, 2),
            PageRequest.of(0, 2),
            5
        );

        ApiListResponse<String> response = ResponseFactory.page(
            page,
            mapper()
        );

        assertThat(response.data()).containsExactly("v1", "v2");

        assertThat(response.meta()).isNotNull();
        assertThat(response.meta().page()).isEqualTo(0);
        assertThat(response.meta().size()).isEqualTo(2);
        assertThat(response.meta().total()).isEqualTo(5);
    }

    @Test
    void pageHandlesEmptyPage() {
        Page<Integer> page = Page.empty();

        ApiListResponse<String> response = ResponseFactory.page(
            page,
            mapper()
        );

        assertThat(response.data()).isEmpty();
        assertThat(response.meta()).isNotNull();
    }
}