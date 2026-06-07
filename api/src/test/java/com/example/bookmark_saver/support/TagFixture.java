package com.example.bookmark_saver.support;

import com.example.bookmark_saver.domain.Tag;

/**
 * Test fixture for {@link Tag} instances.
 */
public class TagFixture {
    public static Tag withDefaults() {
        return create(1L, "Java", "java");
    }

    public static Tag withId(Long id) {
        return create(id, "Java", "java");
    }

    public static Tag create(Long id, String name, String slug) {
        Tag tag = new Tag();

        tag.setName(name);
        tag.setSlug(slug);

        try {
            var field = Tag.class.getDeclaredField("id");

            field.setAccessible(true);
            field.set(tag, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        
        return tag;
    }
}
