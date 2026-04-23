package com.example.bookmark_saver.support;

import com.example.bookmark_saver.domain.BookmarkList;

/**
 * Test fixture for {@link BookmarkList} instances.
 */
public class BookmarkListFixture {
    public static BookmarkList withDefaults() {
        return create(1L, "My List", null);
    }

    public static BookmarkList withId(Long id) {
        return create(id, "My List", null);
    }

    public static BookmarkList withName(String name) {
        return create(1L, name, null);
    }

    public static BookmarkList create(Long id, String name, String description) {
        BookmarkList list = new BookmarkList();

        list.setName(name);
        list.setDescription(description);

        try {
            var field = BookmarkList.class.getDeclaredField("id");

            field.setAccessible(true);
            field.set(list, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        return list;
    }
}
