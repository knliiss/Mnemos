CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE public_profiles
(
    account_id          UUID PRIMARY KEY,
    cristalix_uuid      UUID         NOT NULL UNIQUE,
    cristalix_nickname  VARCHAR(64)  NOT NULL,
    likes               BIGINT       NOT NULL DEFAULT 0,
    dislikes            BIGINT       NOT NULL DEFAULT 0,
    comments            BIGINT       NOT NULL DEFAULT 0,
    social_revision     BIGINT       NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL,

    CONSTRAINT ck_public_profiles_social_counts
        CHECK (likes >= 0 AND dislikes >= 0 AND comments >= 0),
    CONSTRAINT ck_public_profiles_social_revision
        CHECK (social_revision >= 0)
);

CREATE INDEX idx_public_profiles_nickname_trgm
    ON public_profiles
        USING gin (cristalix_nickname gin_trgm_ops);
