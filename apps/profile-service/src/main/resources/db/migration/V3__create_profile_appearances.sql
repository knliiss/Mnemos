CREATE TABLE profile_appearances
(
    account_id UUID PRIMARY KEY,
    revision   BIGINT      NOT NULL,
    appearance JSONB       NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
