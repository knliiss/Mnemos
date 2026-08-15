CREATE TABLE outbox_events
(
    id              UUID         PRIMARY KEY,
    topic           VARCHAR(249) NOT NULL,
    message_key     VARCHAR(512) NOT NULL,
    producer        VARCHAR(128) NOT NULL,
    event_type      VARCHAR(150) NOT NULL,
    event_version   INT          NOT NULL,
    aggregate_type  VARCHAR(150) NOT NULL,
    aggregate_id    UUID         NOT NULL,
    occurred_at     TIMESTAMPTZ  NOT NULL,
    metadata_json   TEXT         NOT NULL,
    payload_json    TEXT         NOT NULL,
    status          VARCHAR(20)  NOT NULL,
    attempt_count   INT          NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ  NOT NULL,
    claimed_by      VARCHAR(200),
    claim_until     TIMESTAMPTZ,
    sent_at         TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL,

    CONSTRAINT ck_outbox_status
        CHECK (status IN ('PENDING', 'SENDING', 'SENT', 'FAILED')),
    CONSTRAINT ck_outbox_topic_not_blank
        CHECK (BTRIM(topic) <> ''),
    CONSTRAINT ck_outbox_message_key_not_blank
        CHECK (BTRIM(message_key) <> ''),
    CONSTRAINT ck_outbox_producer_not_blank
        CHECK (BTRIM(producer) <> ''),
    CONSTRAINT ck_outbox_event_type_not_blank
        CHECK (BTRIM(event_type) <> ''),
    CONSTRAINT ck_outbox_event_version_positive
        CHECK (event_version > 0),
    CONSTRAINT ck_outbox_aggregate_type_not_blank
        CHECK (BTRIM(aggregate_type) <> ''),
    CONSTRAINT ck_outbox_attempt_count_non_negative
        CHECK (attempt_count >= 0)
);

CREATE INDEX idx_outbox_events_ready
    ON outbox_events (status, next_attempt_at, created_at);

CREATE INDEX idx_outbox_events_claim
    ON outbox_events (status, claim_until);

CREATE INDEX idx_outbox_events_aggregate
    ON outbox_events (aggregate_type, aggregate_id, created_at);

CREATE INDEX idx_outbox_events_sent_at
    ON outbox_events (sent_at);
