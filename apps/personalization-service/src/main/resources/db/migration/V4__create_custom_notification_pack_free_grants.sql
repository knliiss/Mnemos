CREATE TABLE custom_notification_pack_free_grants
(
    user_id    UUID PRIMARY KEY,
    claimed_at TIMESTAMPTZ NOT NULL
);
