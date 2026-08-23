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
