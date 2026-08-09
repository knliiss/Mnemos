ALTER TABLE users
    ADD COLUMN active_account_id UUID;

ALTER TABLE users
    ADD CONSTRAINT fk_users_active_account_membership
        FOREIGN KEY (id, active_account_id)
            REFERENCES account_members (user_id, account_id)
            DEFERRABLE INITIALLY DEFERRED;

CREATE INDEX idx_users_active_account_id
    ON users (active_account_id);