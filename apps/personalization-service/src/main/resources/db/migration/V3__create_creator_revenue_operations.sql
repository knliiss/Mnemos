CREATE TABLE creator_revenue_operations
(
    id                    UUID         PRIMARY KEY,
    request_id            UUID         NOT NULL,
    purchase_operation_id UUID         NOT NULL,
    creator_user_id       UUID         NOT NULL,
    amount_prime          BIGINT       NOT NULL,
    status                VARCHAR(16)  NOT NULL,
    wallet_transaction_id UUID,
    failure_reason        VARCHAR(128),
    next_retry_at         TIMESTAMPTZ,
    created_at            TIMESTAMPTZ  NOT NULL,

    CONSTRAINT uq_creator_revenue_operations_request_id
        UNIQUE (request_id),

    CONSTRAINT uq_creator_revenue_operations_purchase_operation_id
        UNIQUE (purchase_operation_id),

    CONSTRAINT fk_creator_revenue_operations_purchase_operation
        FOREIGN KEY (purchase_operation_id)
            REFERENCES purchase_operations (id),

    CONSTRAINT ck_creator_revenue_operations_amount_prime
        CHECK (amount_prime > 0),

    CONSTRAINT ck_creator_revenue_operations_status
        CHECK (status IN ('PENDING', 'CONFIRMED', 'FAILED')),

    CONSTRAINT ck_creator_revenue_operations_retry_state
        CHECK (
            (status = 'FAILED' AND next_retry_at IS NOT NULL)
            OR (status IN ('PENDING', 'CONFIRMED') AND next_retry_at IS NULL)
        )
);

CREATE INDEX idx_creator_revenue_operations_creator_created_at
    ON creator_revenue_operations (creator_user_id, created_at DESC, id DESC);

CREATE INDEX idx_creator_revenue_operations_retry
    ON creator_revenue_operations (next_retry_at, id)
    WHERE status = 'FAILED';
