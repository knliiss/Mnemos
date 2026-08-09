ALTER TABLE accounts
    ADD COLUMN max_members INT NOT NULL,
    ADD CONSTRAINT ck_accounts_max_members_positive
        CHECK (max_members > 0);