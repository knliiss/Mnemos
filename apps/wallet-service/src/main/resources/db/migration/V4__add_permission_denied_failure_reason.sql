ALTER TABLE transaction_requests
    DROP CONSTRAINT ck_transaction_requests_failure_reason;

ALTER TABLE transaction_requests
    ADD CONSTRAINT ck_transaction_requests_failure_reason
        CHECK (
            failure_reason IS NULL
                OR failure_reason IN (
                                      'INSUFFICIENT_BALANCE',
                                      'INVALID_AMOUNT',
                                      'BALANCE_LIMIT_EXCEEDED',
                                      'IDEMPOTENCY_CONFLICT',
                                      'PERMISSION_DENIED',
                                      'SOURCE_REFERENCE_NOT_FOUND',
                                      'TRANSACTION_NOT_REFUNDABLE',
                                      'TRANSACTION_ALREADY_REFUNDED'
                )
            );