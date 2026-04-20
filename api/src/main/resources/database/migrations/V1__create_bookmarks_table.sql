CREATE TABLE bookmarks (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    url VARCHAR(2048) NOT NULL,
    notes TEXT,
    favorite BOOLEAN NOT NULL DEFAULT FALSE,
    metadata JSONB,
    metadata_status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_bookmarks_favorite ON bookmarks(favorite);

CREATE INDEX idx_bookmark_metadata_domain ON bookmarks ((metadata->>'domain'));
