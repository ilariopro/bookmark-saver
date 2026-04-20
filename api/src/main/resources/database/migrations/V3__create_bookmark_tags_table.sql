CREATE TABLE bookmark_tags (
    bookmark_id BIGINT NOT NULL,
    tag_id BIGINT NOT NULL,

    PRIMARY KEY (bookmark_id, tag_id),

    CONSTRAINT fk_bookmark_tags_bookmark
        FOREIGN KEY (bookmark_id)
        REFERENCES bookmarks(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_bookmark_tags_tag
        FOREIGN KEY (tag_id)
        REFERENCES tags(id)
        ON DELETE CASCADE
);
