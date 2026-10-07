CREATE TABLE bot_agent_instances (
    id UUID PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    os_username VARCHAR(100),
    machine_name VARCHAR(100),
    fingerprint CHAR(64),
    secret_hash CHAR(64) NOT NULL UNIQUE,
    first_seen_at TIMESTAMPTZ,
    last_seen_at TIMESTAMPTZ,
    disconnected_at TIMESTAMPTZ,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    CONSTRAINT ck_bot_agent_fingerprint CHECK (fingerprint IS NULL OR fingerprint ~ '^[a-f0-9]{64}$'),
    CONSTRAINT ck_bot_agent_secret CHECK (secret_hash ~ '^[a-f0-9]{64}$')
);
CREATE INDEX idx_bot_agent_last_seen ON bot_agent_instances (last_seen_at DESC);
