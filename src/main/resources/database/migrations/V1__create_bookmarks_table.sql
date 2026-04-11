CREATE TABLE bookmarks (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    url VARCHAR(2048) NOT NULL,
    notes TEXT,
    favorite BOOLEAN NOT NULL DEFAULT FALSE,

    meta_title VARCHAR(255),
    meta_description VARCHAR(500),
    meta_image_url VARCHAR(2048),

    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_bookmarks_favorite ON bookmarks(favorite);
