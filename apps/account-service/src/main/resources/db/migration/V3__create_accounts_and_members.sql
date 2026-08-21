CREATE TABLE accounts
(
    id                 UUID        NOT NULL,
    owner_member_id    UUID        NOT NULL,
    cristalix_uuid     UUID        NOT NULL,
    cristalix_nickname VARCHAR(20) NOT NULL,
    max_members        INT         NOT NULL,
    status             VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    deactivated_at     TIMESTAMPTZ,
    created_at         TIMESTAMPTZ NOT NULL,
    updated_at         TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_accounts
        PRIMARY KEY (id),

    CONSTRAINT uq_accounts_cristalix_uuid
        UNIQUE (cristalix_uuid),

    CONSTRAINT ck_accounts_max_members_positive
        CHECK (max_members > 0),

    CONSTRAINT ck_accounts_status
        CHECK (status IN ('ACTIVE', 'DEACTIVATED')),

    CONSTRAINT ck_accounts_deactivation_date
        CHECK (
            (status = 'ACTIVE' AND deactivated_at IS NULL)
            OR
            (status = 'DEACTIVATED' AND deactivated_at IS NOT NULL)
        )
);

CREATE TABLE account_members
(
    id         UUID        NOT NULL,
    account_id UUID        NOT NULL,
    user_id    UUID        NOT NULL,
    joined_at  TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_account_members
        PRIMARY KEY (id),

    CONSTRAINT uq_account_members_user_account
        UNIQUE (user_id, account_id),

    CONSTRAINT uq_account_members_account_id
        UNIQUE (account_id, id),

    CONSTRAINT fk_account_members_account
        FOREIGN KEY (account_id)
            REFERENCES accounts (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_account_members_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
);

ALTER TABLE accounts
    ADD CONSTRAINT fk_accounts_owner_member
        FOREIGN KEY (id, owner_member_id)
            REFERENCES account_members (account_id, id)
            DEFERRABLE INITIALLY DEFERRED;

ALTER TABLE users
    ADD COLUMN active_account_id UUID,
    ADD CONSTRAINT fk_users_active_account_membership
        FOREIGN KEY (id, active_account_id)
            REFERENCES account_members (user_id, account_id)
            DEFERRABLE INITIALLY DEFERRED;

CREATE INDEX idx_account_members_user_id
    ON account_members (user_id);

CREATE INDEX idx_users_active_account_id
    ON users (active_account_id);
