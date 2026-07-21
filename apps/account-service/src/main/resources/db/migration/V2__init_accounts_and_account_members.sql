CREATE TABLE accounts
(
    id                 UUID         NOT NULL,
    owner_user_id      UUID         NOT NULL,
    cristalix_uuid     UUID         NOT NULL,
    cristalix_nickname VARCHAR(20) NOT NULL,
    created_at         TIMESTAMPTZ  NOT NULL,
    updated_at         TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_accounts PRIMARY KEY (id),
    CONSTRAINT uq_accounts_cristalix_uuid UNIQUE (cristalix_uuid),
    CONSTRAINT fk_accounts_owner_user
        FOREIGN KEY (owner_user_id)
            REFERENCES users (id)
);

CREATE TABLE account_members
(
    id         UUID        NOT NULL,
    account_id UUID        NOT NULL,
    user_id    UUID        NOT NULL,
    joined_at  TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_account_members PRIMARY KEY (id),
    CONSTRAINT uq_account_members_account_user
        UNIQUE (user_id, account_id),
    CONSTRAINT fk_account_members_account
        FOREIGN KEY (account_id)
            REFERENCES accounts (id)
            ON DELETE CASCADE,
    CONSTRAINT fk_account_members_user
        FOREIGN KEY (user_id)
            REFERENCES users (id)
            ON DELETE CASCADE
);

CREATE INDEX idx_account_members_user_id
    ON account_members (user_id);