ALTER TABLE drop_spotlights
    ADD COLUMN idempotency_key uuid;

CREATE UNIQUE INDEX uq_drop_spotlights_idempotency_key
    ON drop_spotlights (idempotency_key)
    WHERE idempotency_key IS NOT NULL;
