ALTER TABLE personalizations
    ALTER COLUMN creator_user_id DROP NOT NULL,
    ADD COLUMN catalog_key VARCHAR(96);

ALTER TABLE personalizations
    DROP CONSTRAINT ck_personalizations_origin,
    DROP CONSTRAINT ck_personalizations_lifecycle_state;

ALTER TABLE personalizations
    ADD CONSTRAINT ck_personalizations_origin
        CHECK (origin IN ('SYSTEM', 'CREATOR', 'CUSTOM')),
    ADD CONSTRAINT ck_personalizations_identity
        CHECK (
            (
                origin = 'SYSTEM'
                AND creator_user_id IS NULL
                AND catalog_key IS NOT NULL
            )
            OR
            (
                origin IN ('CREATOR', 'CUSTOM')
                AND creator_user_id IS NOT NULL
                AND catalog_key IS NULL
            )
        ),
    ADD CONSTRAINT ck_personalizations_catalog_key
        CHECK (
            catalog_key IS NULL
            OR catalog_key ~ '^[a-z0-9][a-z0-9.-]{2,95}$'
        ),
    ADD CONSTRAINT ck_personalizations_lifecycle_state
        CHECK (
            (
                status = 'DRAFT'
                AND origin = 'CREATOR'
                AND visibility = 'PRIVATE'
                AND rejection_reason IS NULL
            )
            OR
            (
                status = 'PRIVATE'
                AND origin = 'CUSTOM'
                AND visibility = 'PRIVATE'
                AND rejection_reason IS NULL
            )
            OR
            (
                status = 'IN_REVIEW'
                AND origin IN ('CREATOR', 'CUSTOM')
                AND visibility = 'PRIVATE'
                AND rejection_reason IS NULL
            )
            OR
            (
                status = 'REJECTED'
                AND origin IN ('CREATOR', 'CUSTOM')
                AND visibility = 'PRIVATE'
                AND rejection_reason IS NOT NULL
                AND BTRIM(rejection_reason) <> ''
            )
            OR
            (
                status = 'APPROVED'
                AND origin IN ('CREATOR', 'CUSTOM')
                AND visibility = 'PUBLIC'
                AND price_currency IS NOT NULL
                AND price_amount IS NOT NULL
                AND rejection_reason IS NULL
            )
            OR
            (
                status IN ('PUBLISHED', 'UNLISTED')
                AND visibility = 'PUBLIC'
                AND price_currency IS NOT NULL
                AND price_amount IS NOT NULL
                AND rejection_reason IS NULL
            )
        );

CREATE UNIQUE INDEX uq_personalizations_catalog_key
    ON personalizations (catalog_key)
    WHERE catalog_key IS NOT NULL;

CREATE TABLE builtin_personalization_catalog_state
(
    id         VARCHAR(16) PRIMARY KEY,
    revision   INTEGER     NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT ck_builtin_personalization_catalog_state_id
        CHECK (id = 'main'),
    CONSTRAINT ck_builtin_personalization_catalog_state_revision
        CHECK (revision >= 0)
);

INSERT INTO builtin_personalization_catalog_state (id, revision, updated_at)
VALUES ('main', 0, TIMESTAMPTZ '1970-01-01 00:00:00+00');
