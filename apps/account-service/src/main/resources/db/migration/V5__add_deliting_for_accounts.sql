ALTER TABLE accounts
    ADD COLUMN status VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    ADD COLUMN deletion_requested_at TIMESTAMPTZ,
    ADD COLUMN deleted_at TIMESTAMPTZ;

ALTER TABLE accounts
    ADD CONSTRAINT chk_accounts_status
        CHECK (
            status IN (
                       'ACTIVE',
                       'DELETING',
                       'DELETED'
                )
            );

ALTER TABLE accounts
    ADD CONSTRAINT chk_accounts_deletion_dates
        CHECK (
            (status = 'ACTIVE'
                AND deletion_requested_at IS NULL
                AND deleted_at IS NULL)
                OR
            (status = 'DELETING'
                AND deletion_requested_at IS NOT NULL
                AND deleted_at IS NULL)
                OR
            (status = 'DELETED'
                AND deletion_requested_at IS NOT NULL
                AND deleted_at IS NOT NULL)
            );