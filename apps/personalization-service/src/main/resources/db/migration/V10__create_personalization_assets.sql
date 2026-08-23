CREATE TABLE personalization_assets
(
    id            UUID PRIMARY KEY,
    owner_user_id UUID         NOT NULL,
    object_key    VARCHAR(160) NOT NULL UNIQUE,
    content_type  VARCHAR(64)  NOT NULL,
    size_bytes    BIGINT       NOT NULL,
    width_px      INTEGER      NOT NULL,
    height_px     INTEGER      NOT NULL,
    created_at    TIMESTAMPTZ  NOT NULL,

    CONSTRAINT ck_personalization_assets_object_key
        CHECK (object_key ~ '^personalizations/[0-9a-fA-F-]{36}/[0-9a-fA-F-]{36}\.png$'),
    CONSTRAINT ck_personalization_assets_content_type
        CHECK (content_type = 'image/png'),
    CONSTRAINT ck_personalization_assets_size
        CHECK (size_bytes > 0 AND size_bytes <= 2097152),
    CONSTRAINT ck_personalization_assets_dimensions
        CHECK (
            width_px BETWEEN 16 AND 2048
            AND height_px BETWEEN 16 AND 2048
        )
);

CREATE INDEX idx_personalization_assets_owner_created_at
    ON personalization_assets (owner_user_id, created_at DESC, id DESC);
