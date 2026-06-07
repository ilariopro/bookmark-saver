CREATE TABLE tags (
    id               BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    parent_id        BIGINT REFERENCES tags(id) ON DELETE SET NULL,
    name             VARCHAR(255) NOT NULL,
    slug             VARCHAR(260) NOT NULL UNIQUE,
    background_color VARCHAR(255),
    text_color       VARCHAR(255),
    created_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at       TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tags_name ON tags(name);

CREATE INDEX idx_tags_parent_id ON tags(parent_id);