ALTER TABLE accounts
    ADD COLUMN base_role_id UUID;

ALTER TABLE accounts
    ADD CONSTRAINT fk_accounts_base_role
        FOREIGN KEY (id, base_role_id)
            REFERENCES roles (account_id, id);

CREATE INDEX idx_accounts_base_role_id
    ON accounts (base_role_id)
    WHERE base_role_id IS NOT NULL;