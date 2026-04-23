package com.example.bookmark_saver.utility;

import java.util.Arrays;
import java.util.List;

/**
 * Parses comma-separated input into a clean list of distinct, trimmed values.
 */
public class CommaSeparatedParser {
    /**
     * Parses a comma-separated string into a normalized list of tokens.
     * 
     * Handles null input, trims whitespace, removes blank entries and duplicates.
     *
     * @param value The raw comma-separated string (e.g. "java, spring, ,java").
     * @return List of normalized strings, or an empty list if value is null or blank.
     */
    public static List<String> parse(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }

        return Arrays.stream(value.split(","))
            .map(String::strip)
            .filter(string -> !string.isBlank())
            .distinct()
            .toList();
    }
}
