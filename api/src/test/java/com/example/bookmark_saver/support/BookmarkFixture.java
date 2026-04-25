package com.example.bookmark_saver.support;

import com.example.bookmark_saver.domain.Bookmark;

/**
 * Test fixture for {@link Bookmark} instances.
 */
public class BookmarkFixture {
    public static Bookmark withDefaults() {
        return create(1L, "https://example.com", null);
    }

    public static Bookmark withId(Long id) {
        return create(id, "https://example.com", null);
    }

    public static Bookmark withUrl(String url) {
        return create(1L, url, null);
    }

    public static Bookmark create(Long id, String url, String notes) {
        Bookmark bookmark = new Bookmark();

        bookmark.setUrl(url);
        bookmark.setNotes(notes);
        
        try {
            var field = Bookmark.class.getDeclaredField("id");

            field.setAccessible(true);
            field.set(bookmark, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return bookmark;
    }
}