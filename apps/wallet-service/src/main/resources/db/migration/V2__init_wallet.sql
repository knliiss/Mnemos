CREATE TABLE transactions
(
    id                  uuid PRIMARY KEY,
    account_id          uuid        NOT NULL,
    currency            VARCHAR(16) NOT NULL,
    type                VARCHAR(16) NOT NULL,
    source              VARCHAR(64) NOT NULL,
    source_reference_id uuid        NOT NULL,
    amount              BIGINT      NOT NULL,
    request_id          uuid        NOT NULL,
    created_at          timestamptz NOT NULL,

    CONSTRAINT uq_transactions_request_id
        UNIQUE (request_id),

    CONSTRAINT ck_transactions_currency
        CHECK ( currency IN ('COIN', 'PRIME')),

    CONSTRAINT ck_transactions_type
        CHECK ( type IN ('DEBIT', 'CREDIT')),

    CONSTRAINT ck_transactions_amount
        CHECK ( amount > 0),

    CONSTRAINT ck_transactions_source
        CHECK (source IN (
                          'DAILY_REWARD',
                          'BET_PLACE',
                          'BET_WIN',
                          'BET_REFUND',
                          'PERSONALIZATION_PURCHASE',
                          'PERSONALIZATION_REFUND',
                          'PRIME_PURCHASE',
                          'ADMIN_ADJUSTMENT'
            ))
);

CREATE INDEX idx_transactions_account_currency_created_at
    ON transactions (account_id, currency, created_at DESC, id DESC);

CREATE INDEX idx_transactions_source_reference
    ON transactions (source, source_reference_id);

CREATE TABLE balances
(
    account_id          uuid        NOT NULL,
    currency            VARCHAR(16) NOT NULL,
    amount              BIGINT      NOT NULL,
    last_transaction_id uuid,
    created_at          timestamptz NOT NULL,
    updated_at          timestamptz NOT NULL,

    CONSTRAINT pk_balances
        PRIMARY KEY (account_id, currency),

    CONSTRAINT fk_balances_last_transaction
        FOREIGN KEY (last_transaction_id)
            REFERENCES transactions (id)
            DEFERRABLE INITIALLY DEFERRED,

    CONSTRAINT ck_balances_currency
        CHECK ( currency IN ('COIN', 'PRIME')),

    CONSTRAINT ck_balances_amount
        CHECK ( amount >= 0 )
);

CREATE INDEX idx_balances_amount
    ON balances (amount);
