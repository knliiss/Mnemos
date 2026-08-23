ALTER TABLE collector_credentials
    ADD COLUMN owner_user_id UUID;

CREATE INDEX idx_collector_credentials_owner_user_id
    ON collector_credentials (owner_user_id)
    WHERE owner_user_id IS NOT NULL;

CREATE TABLE collector_activations
(
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    token_hash VARCHAR(64) NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    credential_id UUID,
    redeemed_at TIMESTAMPTZ,

    CONSTRAINT uq_collector_activations_token_hash
        UNIQUE (token_hash),
    CONSTRAINT fk_collector_activations_credential
        FOREIGN KEY (credential_id)
            REFERENCES collector_credentials (id),
    CONSTRAINT ck_collector_activations_token_hash
        CHECK (token_hash ~ '^[0-9a-f]{64}$'),
    CONSTRAINT ck_collector_activations_expiration
        CHECK (expires_at > created_at),
    CONSTRAINT ck_collector_activations_redemption
        CHECK (
            (credential_id IS NULL AND redeemed_at IS NULL)
            OR
            (credential_id IS NOT NULL AND redeemed_at IS NOT NULL)
        )
);

CREATE INDEX idx_collector_activations_user_id
    ON collector_activations (user_id);

CREATE INDEX idx_collector_activations_expires_at
    ON collector_activations (expires_at)
    WHERE redeemed_at IS NULL;
