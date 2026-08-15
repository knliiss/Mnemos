ALTER TABLE wallet_operations
    ADD COLUMN initiated_by_user_id UUID;

CREATE INDEX idx_wallet_operations_initiator
    ON wallet_operations (initiated_by_user_id, created_at DESC)
    WHERE initiated_by_user_id IS NOT NULL;
