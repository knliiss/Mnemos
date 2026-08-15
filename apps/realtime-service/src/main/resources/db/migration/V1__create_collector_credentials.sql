CREATE TABLE collector_credentials
(
    id UUID PRIMARY KEY,
    name VARCHAR(64) NOT NULL,
    secret_hash VARCHAR(64) NOT NULL,
    enabled BOOLEAN NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,
    revoked_at TIMESTAMPTZ,

    CONSTRAINT uq_collector_credentials_name
        UNIQUE (name),
    CONSTRAINT ck_collector_credentials_name_not_blank
        CHECK (LENGTH(TRIM(name)) > 0),
    CONSTRAINT ck_collector_credentials_secret_hash
        CHECK (secret_hash ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_collector_credentials_revocation
        CHECK (revoked_at IS NULL OR enabled = FALSE)
);

CREATE INDEX idx_collector_credentials_enabled
    ON collector_credentials (enabled)
    WHERE enabled = TRUE;
