CREATE TABLE outbox_events
(
    id UUID PRIMARY KEY,
    sequence_no BIGINT GENERATED ALWAYS AS IDENTITY UNIQUE,
    topic VARCHAR(255) NOT NULL,
    event_key VARCHAR(255) NOT NULL,
    event_type VARCHAR(150) NOT NULL,
    event_version INTEGER NOT NULL,
    producer VARCHAR(100) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id UUID NOT NULL,
    correlation_id VARCHAR(255),
    causation_id UUID,
    actor_user_id UUID,
    envelope JSONB NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(16) NOT NULL DEFAULT 'PENDING',
    failure_count INTEGER NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL,
    locked_by VARCHAR(255),
    locked_until TIMESTAMPTZ,
    published_at TIMESTAMPTZ,
    dead_at TIMESTAMPTZ,
    last_error TEXT
);

CREATE INDEX idx_moderation_outbox_pending
    ON outbox_events (next_attempt_at, sequence_no)
    WHERE status = 'PENDING';
