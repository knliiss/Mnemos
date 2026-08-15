ALTER TABLE personalizations
    ADD CONSTRAINT ck_personalizations_custom_origin_type
        CHECK (
            origin <> 'CUSTOM'
            OR type = 'NOTIFICATION_PACK'
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
                AND visibility = 'PRIVATE'
                AND rejection_reason IS NULL
            )
            OR
            (
                status = 'REJECTED'
                AND visibility = 'PRIVATE'
                AND rejection_reason IS NOT NULL
                AND BTRIM(rejection_reason) <> ''
            )
            OR
            (
                status IN ('APPROVED', 'PUBLISHED', 'UNLISTED')
                AND visibility = 'PUBLIC'
                AND price_currency IS NOT NULL
                AND price_amount IS NOT NULL
                AND rejection_reason IS NULL
            )
        );
