CREATE TABLE active_notification_packs
(
    user_id               UUID         PRIMARY KEY,
    personalization_id    UUID,
    mixed_aggregate_title VARCHAR(512),
    templates             JSONB,
    last_revision         BIGINT       NOT NULL,
    last_event_id         UUID         NOT NULL,
    last_event_at         TIMESTAMPTZ  NOT NULL,

    CONSTRAINT ck_active_notification_packs_revision
        CHECK (last_revision >= 0),

    CONSTRAINT ck_active_notification_packs_snapshot
        CHECK (
            (
                personalization_id IS NULL
                AND mixed_aggregate_title IS NULL
                AND templates IS NULL
            )
            OR
            (
                personalization_id IS NOT NULL
                AND mixed_aggregate_title IS NOT NULL
                AND templates IS NOT NULL
            )
        )
);
