CREATE TABLE active_notification_packs
(
    user_id               UUID         PRIMARY KEY,
    personalization_id    UUID         NOT NULL,
    mixed_aggregate_title VARCHAR(512) NOT NULL,
    templates             JSONB        NOT NULL,
    updated_at            TIMESTAMPTZ  NOT NULL
);
