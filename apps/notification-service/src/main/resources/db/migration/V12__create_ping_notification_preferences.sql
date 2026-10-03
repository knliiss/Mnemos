CREATE TABLE ping_notification_preferences
(
    user_id    UUID PRIMARY KEY,
    enabled    BOOLEAN     NOT NULL,
    updated_at TIMESTAMPTZ NOT NULL
);
