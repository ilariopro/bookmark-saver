package com.example.bookmark_saver.utility;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

public class CommaSeparatedParserTest {
    @Test
    void returnsEmptyListWhenNull() {
        assertThat(CommaSeparatedParser.parse(null))
            .isEmpty();
    }

    @Test
    void returnsEmptyListWhenBlank() {
        assertThat(CommaSeparatedParser.parse("   "))
            .isEmpty();
    }

    @Test
    void trimsWhitespace() {
        assertThat(CommaSeparatedParser.parse("java, spring"))
            .containsExactly("java", "spring");
    }

    @Test
    void ignoresBlanks() {
        assertThat(CommaSeparatedParser.parse("java, ,spring"))
            .containsExactly("java", "spring");
    }

    @Test
    void ignoreDuplicates() {
        assertThat(CommaSeparatedParser.parse("java,java"))
            .containsExactly("java");
    }
}
