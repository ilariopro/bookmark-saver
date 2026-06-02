CREATE TABLE tags (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    parent_id BIGINT REFERENCES tags(id) ON DELETE SET NULL,
    background_color VARCHAR(255),
    text_color VARCHAR(255),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_tags_name ON tags(name);

CREATE INDEX idx_tags_parent_id ON tags(parent_id);

CREATE UNIQUE INDEX uq_tags_parent_name ON tags(parent_id, name);