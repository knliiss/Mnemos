ALTER TABLE accounts
    ADD COLUMN deleted_at TIMESTAMPTZ;

ALTER TABLE accounts
    DROP CONSTRAINT ck_accounts_status,
    DROP CONSTRAINT ck_accounts_deactivation_date,
    DROP CONSTRAINT uq_accounts_cristalix_uuid;

ALTER TABLE accounts
    ADD CONSTRAINT ck_accounts_status
        CHECK (status IN ('ACTIVE', 'DEACTIVATED', 'DELETED')),
    ADD CONSTRAINT ck_accounts_lifecycle_dates
        CHECK (
            (status = 'ACTIVE' AND deactivated_at IS NULL AND deleted_at IS NULL)
            OR
            (status = 'DEACTIVATED' AND deactivated_at IS NOT NULL AND deleted_at IS NULL)
            OR
            (status = 'DELETED' AND deleted_at IS NOT NULL)
        );

CREATE UNIQUE INDEX uq_accounts_live_cristalix_uuid
    ON accounts (cristalix_uuid)
    WHERE status <> 'DELETED';
