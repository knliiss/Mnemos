CREATE TABLE account_invites
(
    id                 UUID PRIMARY KEY,
    account_id         UUID        NOT NULL,
    code               VARCHAR(32) NOT NULL,
    created_by_user_id UUID        NOT NULL,
    created_at         TIMESTAMPTZ NOT NULL,
    expires_at         TIMESTAMPTZ,
    consumed_at        TIMESTAMPTZ,
    revoked_at         TIMESTAMPTZ,

    CONSTRAINT uq_account_invites_code
        UNIQUE (code),
    CONSTRAINT fk_account_invites_account_id
        FOREIGN KEY (account_id)
            REFERENCES accounts (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_account_invites_created_by_user_id
        FOREIGN KEY (created_by_user_id)
            REFERENCES users (id)
            ON DELETE CASCADE,
    CONSTRAINT ck_account_invites_state
        CHECK (
            consumed_at IS NULL
            OR revoked_at IS NULL
            )
);

CREATE INDEX  idx_account_invites_account_created_at
    ON account_invites (account_id, created_at DESC)