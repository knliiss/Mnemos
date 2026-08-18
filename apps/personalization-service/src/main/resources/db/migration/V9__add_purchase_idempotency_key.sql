ALTER TABLE purchase_operations
    ADD COLUMN idempotency_key uuid;

CREATE UNIQUE INDEX uq_purchase_operations_idempotency_key
    ON purchase_operations (idempotency_key)
    WHERE idempotency_key IS NOT NULL;
