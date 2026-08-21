CREATE TABLE notification_suspensions
(
    sanction_id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    active BOOLEAN NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_notification_suspensions_user_active
    ON notification_suspensions (user_id)
    WHERE active = TRUE;
