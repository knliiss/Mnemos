ALTER TABLE accounts
    ADD COLUMN deactivated_at TIMESTAMPTZ;

UPDATE accounts
SET deactivated_at = COALESCE(deleted_at, deletion_requested_at)
WHERE status IN ('DELETING', 'DELETED');

UPDATE accounts
SET status = 'DEACTIVATED'
WHERE status IN ('DELETING', 'DELETED');

ALTER TABLE accounts
    DROP CONSTRAINT chk_accounts_deletion_dates;

ALTER TABLE accounts
    DROP CONSTRAINT chk_accounts_status;

ALTER TABLE accounts
    ADD CONSTRAINT chk_accounts_status
        CHECK (status IN ('ACTIVE', 'DEACTIVATED'));

ALTER TABLE accounts
    ADD CONSTRAINT chk_accounts_deactivation_date
        CHECK (
            (status = 'ACTIVE' AND deactivated_at IS NULL)
            OR
            (status = 'DEACTIVATED' AND deactivated_at IS NOT NULL)
        );

ALTER TABLE accounts
    DROP COLUMN deletion_requested_at,
    DROP COLUMN deleted_at;
