ALTER TABLE wallet_adjustment_operations
    ADD COLUMN idempotency_key uuid;

CREATE UNIQUE INDEX uq_wallet_adjustment_operations_idempotency_key
    ON wallet_adjustment_operations (idempotency_key)
    WHERE idempotency_key IS NOT NULL;
