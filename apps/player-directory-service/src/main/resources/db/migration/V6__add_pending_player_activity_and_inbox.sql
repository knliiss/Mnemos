CREATE TABLE pending_player_activity
(
    normalized_nickname VARCHAR(64) PRIMARY KEY,
    nickname            VARCHAR(64) NOT NULL,
    pending_drop_count  BIGINT      NOT NULL,
    last_drop_at        TIMESTAMPTZ NOT NULL,
    next_attempt_at     TIMESTAMPTZ NOT NULL,
    lease_until         TIMESTAMPTZ,
    consecutive_failures INTEGER    NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ NOT NULL,
    updated_at          TIMESTAMPTZ NOT NULL,

    CONSTRAINT ck_pending_player_activity_drop_count
        CHECK (pending_drop_count > 0),
    CONSTRAINT ck_pending_player_activity_failures
        CHECK (consecutive_failures >= 0)
);

CREATE INDEX idx_pending_player_activity_due
    ON pending_player_activity (next_attempt_at, lease_until);

CREATE TABLE inbox_events
(
    consumer    VARCHAR(150) NOT NULL,
    event_id    UUID         NOT NULL,
    recorded_at TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_inbox_events
        PRIMARY KEY (consumer, event_id),
    CONSTRAINT ck_inbox_consumer_not_blank
        CHECK (LENGTH(TRIM(consumer)) > 0)
);

CREATE INDEX idx_inbox_events_recorded_at
    ON inbox_events (recorded_at);
