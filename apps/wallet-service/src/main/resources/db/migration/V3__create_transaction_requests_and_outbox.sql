CREATE TABLE transaction_requests
(
    request_id          uuid PRIMARY KEY,
    account_id          uuid        NOT NULL,
    currency            VARCHAR(16) NOT NULL,
    type                VARCHAR(16) NOT NULL,
    source              VARCHAR(64) NOT NULL,
    source_reference_id uuid        NOT NULL,
    amount              BIGINT      NOT NULL,
    status              VARCHAR(16) NOT NULL,
    transaction_id      uuid,
    failure_reason      VARCHAR(64),
    created_at          timestamptz NOT NULL,
    completed_at        timestamptz,

    CONSTRAINT uq_transaction_requests_transaction_id
        UNIQUE (transaction_id),

    CONSTRAINT fk_transaction_requests_transaction
        FOREIGN KEY (transaction_id)
            REFERENCES transactions (id)
            DEFERRABLE INITIALLY DEFERRED,

    CONSTRAINT ck_transaction_requests_currency
        CHECK (currency IN ('COIN', 'PRIME')),

    CONSTRAINT ck_transaction_requests_type
        CHECK (type IN ('DEBIT', 'CREDIT')),

    CONSTRAINT ck_transaction_requests_source
        CHECK (source IN (
                          'DAILY_REWARD',
                          'BET_PLACE',
                          'BET_WIN',
                          'BET_REFUND',
                          'PERSONALIZATION_PURCHASE',
                          'PERSONALIZATION_REFUND',
                          'PRIME_PURCHASE',
                          'ADMIN_ADJUSTMENT'
            )),

    CONSTRAINT ck_transaction_requests_status
        CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')),

    CONSTRAINT ck_transaction_requests_failure_reason
        CHECK (
            failure_reason IS NULL
                OR failure_reason IN (
                                      'INSUFFICIENT_BALANCE',
                                      'INVALID_AMOUNT',
                                      'BALANCE_LIMIT_EXCEEDED',
                                      'IDEMPOTENCY_CONFLICT',
                                      'SOURCE_REFERENCE_NOT_FOUND',
                                      'TRANSACTION_NOT_REFUNDABLE',
                                      'TRANSACTION_ALREADY_REFUNDED'
                )
            ),

    CONSTRAINT ck_transaction_requests_result
        CHECK (
            (
                status = 'PENDING'
                    AND transaction_id IS NULL
                    AND failure_reason IS NULL
                    AND completed_at IS NULL
                )
                OR
            (
                status = 'COMPLETED'
                    AND transaction_id IS NOT NULL
                    AND failure_reason IS NULL
                    AND completed_at IS NOT NULL
                )
                OR
            (
                status = 'FAILED'
                    AND transaction_id IS NULL
                    AND failure_reason IS NOT NULL
                    AND completed_at IS NOT NULL
                )
            )
);

CREATE INDEX idx_transaction_requests_account_created_at
    ON transaction_requests (account_id, created_at DESC, request_id DESC);