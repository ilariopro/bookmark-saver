CREATE TABLE bookmark_lists (
    bookmark_id BIGINT NOT NULL,
    list_id BIGINT NOT NULL,

    PRIMARY KEY (bookmark_id, list_id),

    CONSTRAINT fk_bookmark_lists_bookmark
        FOREIGN KEY (bookmark_id)
        REFERENCES bookmarks(id)
        ON DELETE CASCADE,

    CONSTRAINT fk_bookmark_lists_list
        FOREIGN KEY (list_id)
        REFERENCES lists(id)
        ON DELETE CASCADE
);
