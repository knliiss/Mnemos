CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA public;

DO $$
DECLARE
    extension_schema TEXT;
BEGIN
    SELECT namespace.nspname
    INTO extension_schema
    FROM pg_extension extension
             JOIN pg_namespace namespace ON namespace.oid = extension.extnamespace
    WHERE extension.extname = 'pg_trgm';

    IF extension_schema <> 'public' THEN
        EXECUTE 'ALTER EXTENSION pg_trgm SET SCHEMA public';
    END IF;
END
$$;

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
        USING gin (cristalix_nickname public.gin_trgm_ops);

CREATE TABLE pending_social_summaries
(
    account_id   UUID PRIMARY KEY,
    likes        BIGINT      NOT NULL,
    dislikes     BIGINT      NOT NULL,
    comments     BIGINT      NOT NULL,
    revision     BIGINT      NOT NULL,
    occurred_at  TIMESTAMPTZ NOT NULL,

    CONSTRAINT ck_pending_social_summaries_counts
        CHECK (likes >= 0 AND dislikes >= 0 AND comments >= 0),
    CONSTRAINT ck_pending_social_summaries_revision
        CHECK (revision > 0)
);

CREATE INDEX idx_pending_social_summaries_occurred_at
    ON pending_social_summaries (occurred_at);
