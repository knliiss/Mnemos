DROP TABLE IF EXISTS outbox_events;

CREATE TABLE outbox_events
(
    id               UUID PRIMARY KEY,

    topic            VARCHAR(255) NOT NULL,
    event_key        VARCHAR(255) NOT NULL,

    event_type       VARCHAR(150) NOT NULL,
    event_version    INTEGER NOT NULL,

    producer         VARCHAR(100) NOT NULL,

    aggregate_type   VARCHAR(100) NOT NULL,
    aggregate_id     UUID NOT NULL,

    correlation_id   VARCHAR(255),
    causation_id     UUID,
    actor_user_id    UUID,

    envelope         JSONB NOT NULL,

    occurred_at      TIMESTAMPTZ NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL,

    status           VARCHAR(16) NOT NULL DEFAULT 'PENDING',

    attempts         INTEGER NOT NULL DEFAULT 0,
    next_attempt_at  TIMESTAMPTZ NOT NULL,

    locked_by        VARCHAR(255),
    locked_until     TIMESTAMPTZ,

    published_at     TIMESTAMPTZ,
    dead_at          TIMESTAMPTZ,

    last_error       TEXT,

    CONSTRAINT chk_outbox_event_version_positive
        CHECK (event_version > 0),

    CONSTRAINT chk_outbox_attempts_non_negative
        CHECK (attempts >= 0),

    CONSTRAINT chk_outbox_status
        CHECK (
            status IN (
                       'PENDING',
                       'PUBLISHED',
                       'DEAD'
                )
            ),

    CONSTRAINT chk_outbox_lock_consistency
        CHECK (
            (
                locked_by IS NULL
                    AND locked_until IS NULL
                )
                OR
            (
                locked_by IS NOT NULL
                    AND locked_until IS NOT NULL
                )
            ),

    CONSTRAINT chk_outbox_state_consistency
        CHECK (
            (
                status = 'PENDING'
                    AND published_at IS NULL
                    AND dead_at IS NULL
                )
                OR
            (
                status = 'PUBLISHED'
                    AND published_at IS NOT NULL
                    AND dead_at IS NULL
                    AND locked_by IS NULL
                    AND locked_until IS NULL
                )
                OR
            (
                status = 'DEAD'
                    AND published_at IS NULL
                    AND dead_at IS NOT NULL
                    AND locked_by IS NULL
                    AND locked_until IS NULL
                )
            )
);

CREATE INDEX idx_outbox_events_pending
    ON outbox_events (
                      next_attempt_at,
                      created_at,
                      id
        )
    WHERE status = 'PENDING';

CREATE INDEX idx_outbox_events_locked
    ON outbox_events (
                      locked_until
        )
    WHERE status = 'PENDING'
        AND locked_until IS NOT NULL;

CREATE INDEX idx_outbox_events_published_cleanup
    ON outbox_events (
                      published_at
        )
    WHERE status = 'PUBLISHED';

CREATE INDEX idx_outbox_events_dead
    ON outbox_events (
                      dead_at
        )
    WHERE status = 'DEAD';

CREATE INDEX idx_outbox_events_aggregate
    ON outbox_events (
                      aggregate_type,
                      aggregate_id,
                      created_at
        );