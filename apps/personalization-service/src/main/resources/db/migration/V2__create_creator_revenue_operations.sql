CREATE TABLE creator_revenue_operations
(
    id                    UUID PRIMARY KEY,
    request_id            UUID        NOT NULL UNIQUE,
    purchase_operation_id UUID        NOT NULL UNIQUE REFERENCES purchase_operations(id),
    creator_user_id       UUID        NOT NULL,
    amount_prime          BIGINT      NOT NULL,
    status                VARCHAR(16) NOT NULL,
    wallet_transaction_id UUID,
    failure_reason        VARCHAR(128),
    next_retry_at         TIMESTAMPTZ,
    created_at            TIMESTAMPTZ NOT NULL,

    CONSTRAINT ck_creator_revenue_operations_amount_prime
        CHECK (amount_prime > 0),
    CONSTRAINT ck_creator_revenue_operations_status
        CHECK (status IN ('PENDING', 'CONFIRMED', 'FAILED')),
    CONSTRAINT ck_creator_revenue_operations_result_state
        CHECK (
            (
                status = 'PENDING'
                AND wallet_transaction_id IS NULL
                AND failure_reason IS NULL
                AND next_retry_at IS NULL
            )
            OR
            (
                status = 'CONFIRMED'
                AND wallet_transaction_id IS NOT NULL
                AND failure_reason IS NULL
                AND next_retry_at IS NULL
            )
            OR
            (
                status = 'FAILED'
                AND wallet_transaction_id IS NULL
                AND failure_reason IS NOT NULL
                AND BTRIM(failure_reason) <> ''
                AND next_retry_at IS NOT NULL
            )
        )
);

CREATE INDEX idx_creator_revenue_operations_creator_created_at
    ON creator_revenue_operations (creator_user_id, created_at DESC, id DESC);

CREATE INDEX idx_creator_revenue_operations_retry
    ON creator_revenue_operations (next_retry_at, id)
    WHERE status = 'FAILED';
