ALTER TABLE role_assignments
    DROP CONSTRAINT ck_role_assignments_source;

ALTER TABLE role_assignments
    ADD CONSTRAINT ck_role_assignments_source
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
                AND assignment_reason IN ('BASE_ROLE', 'MANUAL')
            )
        );
