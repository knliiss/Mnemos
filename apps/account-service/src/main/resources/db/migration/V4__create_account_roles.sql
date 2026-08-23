CREATE TABLE roles
(
    id             UUID        NOT NULL,
    account_id     UUID        NOT NULL,
    role_name      VARCHAR(32) NOT NULL,
    role_color     VARCHAR(9)  NOT NULL,
    hierarchy_rank BIGINT      NOT NULL,
    created_at     TIMESTAMPTZ NOT NULL,
    updated_at     TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_roles
        PRIMARY KEY (id),

    CONSTRAINT uq_roles_account_name
        UNIQUE (account_id, role_name),

    CONSTRAINT uq_roles_account_id_id
        UNIQUE (account_id, id),

    CONSTRAINT uq_roles_account_hierarchy_rank
        UNIQUE (account_id, hierarchy_rank)
            DEFERRABLE INITIALLY DEFERRED,

    CONSTRAINT fk_roles_account
        FOREIGN KEY (account_id)
            REFERENCES accounts (id)
            ON DELETE CASCADE,

    CONSTRAINT ck_roles_name_length
        CHECK (CHAR_LENGTH(BTRIM(role_name)) BETWEEN 1 AND 32),

    CONSTRAINT ck_roles_color_format
        CHECK (role_color ~ '^#[0-9A-Fa-f]{6}([0-9A-Fa-f]{2})?$')
);

CREATE TABLE role_permissions
(
    role_id    UUID        NOT NULL,
    permission VARCHAR(64) NOT NULL,

    CONSTRAINT pk_role_permissions
        PRIMARY KEY (role_id, permission),

    CONSTRAINT fk_role_permissions_role
        FOREIGN KEY (role_id)
            REFERENCES roles (id)
            ON DELETE CASCADE
);

CREATE TABLE role_assignments
(
    account_id          UUID        NOT NULL,
    account_member_id   UUID        NOT NULL,
    role_id             UUID        NOT NULL,
    assigned_by_type    VARCHAR(16) NOT NULL,
    assigned_by_user_id UUID,
    assignment_reason   VARCHAR(32) NOT NULL,
    assigned_at         TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_role_assignments
        PRIMARY KEY (account_member_id, role_id),

    CONSTRAINT fk_role_assignments_member
        FOREIGN KEY (account_id, account_member_id)
            REFERENCES account_members (account_id, id)
            ON DELETE CASCADE,

    CONSTRAINT fk_role_assignments_role
        FOREIGN KEY (account_id, role_id)
            REFERENCES roles (account_id, id)
            ON DELETE CASCADE,

    CONSTRAINT fk_role_assignments_assigned_by_user
        FOREIGN KEY (assigned_by_user_id)
            REFERENCES users (id),

    CONSTRAINT ck_role_assignments_source
        CHECK (
            (
                assigned_by_type = 'USER'
                AND assigned_by_user_id IS NOT NULL
                AND assignment_reason = 'MANUAL'
            )
            OR
            (
                assigned_by_type = 'SYSTEM'
                AND assigned_by_user_id IS NULL
                AND assignment_reason = 'BASE_ROLE'
            )
        )
);

ALTER TABLE accounts
    ADD COLUMN base_role_id UUID,
    ADD CONSTRAINT fk_accounts_base_role
        FOREIGN KEY (id, base_role_id)
            REFERENCES roles (account_id, id);

CREATE INDEX idx_roles_account_id
    ON roles (account_id);

CREATE INDEX idx_role_assignments_account_id
    ON role_assignments (account_id);

CREATE INDEX idx_role_assignments_role_id
    ON role_assignments (role_id);

CREATE INDEX idx_accounts_base_role_id
    ON accounts (base_role_id)
    WHERE base_role_id IS NOT NULL;
