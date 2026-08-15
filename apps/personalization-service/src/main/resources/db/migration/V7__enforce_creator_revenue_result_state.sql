ALTER TABLE creator_revenue_operations
    ADD CONSTRAINT ck_creator_revenue_operations_result_state
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
        );
