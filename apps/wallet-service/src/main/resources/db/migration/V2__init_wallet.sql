CREATE TABLE transactions
(
    id                  UUID        PRIMARY KEY,
    owner_type          VARCHAR(16) NOT NULL,
    owner_id            UUID        NOT NULL,
    currency            VARCHAR(16) NOT NULL,
    type                VARCHAR(16) NOT NULL,
    source              VARCHAR(64) NOT NULL,
    source_reference_id UUID        NOT NULL,
    amount              BIGINT      NOT NULL,
    request_id          UUID        NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL,

    CONSTRAINT uq_transactions_request_id
        UNIQUE (request_id),

    CONSTRAINT ck_transactions_owner_type
        CHECK (owner_type IN ('ACCOUNT', 'USER')),

    CONSTRAINT ck_transactions_currency
        CHECK (currency IN ('COIN', 'PRIME')),

    CONSTRAINT ck_transactions_owner_currency
        CHECK (
            (owner_type = 'ACCOUNT' AND currency = 'COIN')
                OR
            (owner_type = 'USER' AND currency = 'PRIME')
        ),

    CONSTRAINT ck_transactions_type
        CHECK (type IN ('DEBIT', 'CREDIT')),

    CONSTRAINT ck_transactions_amount
        CHECK (amount > 0),

    CONSTRAINT ck_transactions_source
        CHECK (source IN (
            'DAILY_REWARD',
            'ITEM_DROP_REWARD',
            'BET_PLACE',
            'BET_WIN',
            'BET_REFUND',
            'PERSONALIZATION_PURCHASE',
            'CREATOR_REVENUE',
            'SPOTLIGHT_PURCHASE',
            'PRIME_PURCHASE',
            'ADMIN_ADJUSTMENT'
        ))
);

CREATE INDEX idx_transactions_owner_created_at
    ON transactions (owner_type, owner_id, created_at DESC, id DESC);

CREATE INDEX idx_transactions_source_reference
    ON transactions (source, source_reference_id);

CREATE TABLE balances
(
    owner_type          VARCHAR(16) NOT NULL,
    owner_id            UUID        NOT NULL,
    currency            VARCHAR(16) NOT NULL,
    amount              BIGINT      NOT NULL,
    last_transaction_id UUID,
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_balances
        PRIMARY KEY (owner_type, owner_id, currency),

    CONSTRAINT fk_balances_last_transaction
        FOREIGN KEY (last_transaction_id)
            REFERENCES transactions (id)
            DEFERRABLE INITIALLY DEFERRED,

    CONSTRAINT ck_balances_owner_type
        CHECK (owner_type IN ('ACCOUNT', 'USER')),

    CONSTRAINT ck_balances_currency
        CHECK (currency IN ('COIN', 'PRIME')),

    CONSTRAINT ck_balances_owner_currency
        CHECK (
            (owner_type = 'ACCOUNT' AND currency = 'COIN')
                OR
            (owner_type = 'USER' AND currency = 'PRIME')
        ),

    CONSTRAINT ck_balances_amount
        CHECK (amount >= 0)
);
