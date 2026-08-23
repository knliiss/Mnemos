CREATE TABLE wallet_adjustment_operations
(
    id                    UUID PRIMARY KEY,
    request_id            UUID         NOT NULL UNIQUE,
    idempotency_key       UUID,
    target_type           VARCHAR(16)  NOT NULL,
    target_id             UUID         NOT NULL,
    actor_user_id         UUID         NOT NULL,
    currency              VARCHAR(16)  NOT NULL,
    transaction_type      VARCHAR(16)  NOT NULL,
    amount                BIGINT       NOT NULL CHECK (amount > 0),
    reason                VARCHAR(256) NOT NULL,
    status                VARCHAR(16)  NOT NULL,
    wallet_transaction_id UUID,
    failure_reason        VARCHAR(128),
    created_at            TIMESTAMPTZ  NOT NULL,

    CONSTRAINT ck_wallet_adjustment_target_type
        CHECK (target_type IN ('ACCOUNT', 'USER')),
    CONSTRAINT ck_wallet_adjustment_currency
        CHECK (currency IN ('COIN', 'PRIME')),
    CONSTRAINT ck_wallet_adjustment_target_currency
        CHECK (
            (currency = 'COIN' AND target_type = 'ACCOUNT')
            OR (currency = 'PRIME' AND target_type = 'USER')
        ),
    CONSTRAINT ck_wallet_adjustment_type
        CHECK (transaction_type IN ('DEBIT', 'CREDIT')),
    CONSTRAINT ck_wallet_adjustment_status
        CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')),
    CONSTRAINT ck_wallet_adjustment_reason_not_blank
        CHECK (LENGTH(TRIM(reason)) > 0)
);

CREATE UNIQUE INDEX uq_wallet_adjustment_operations_idempotency_key
    ON wallet_adjustment_operations (idempotency_key)
    WHERE idempotency_key IS NOT NULL;

CREATE INDEX idx_wallet_adjustments_target_created
    ON wallet_adjustment_operations (target_type, target_id, created_at DESC);
