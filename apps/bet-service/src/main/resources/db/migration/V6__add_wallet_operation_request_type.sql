ALTER TABLE wallet_operations
    ADD COLUMN request_type varchar(32),
    ADD COLUMN idempotency_key uuid;

CREATE UNIQUE INDEX uq_wallet_operations_idempotency_key
    ON wallet_operations (idempotency_key)
    WHERE idempotency_key IS NOT NULL;

CREATE INDEX idx_wallet_operations_request_type
    ON wallet_operations (request_type)
    WHERE request_type IS NOT NULL;
