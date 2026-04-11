package com.example.bookmark_saver.dto.request;

import java.util.List;

/**
 * Request payload containing a list of entity IDs.
 */
public record IdListRequest(
    List<Long> ids
) {}