CREATE TABLE comments
(
    id                  UUID PRIMARY KEY,
    target_account_id   UUID         NOT NULL,
    author_user_id      UUID         NOT NULL,
    author_account_id   UUID         NOT NULL,
    parent_comment_id   UUID,
    reply_to_comment_id UUID,
    content             VARCHAR(500) NOT NULL,
    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL,
    deleted_at          TIMESTAMPTZ,

    CONSTRAINT fk_comments_parent FOREIGN KEY (parent_comment_id) REFERENCES comments (id),
    CONSTRAINT fk_comments_reply_target FOREIGN KEY (reply_to_comment_id) REFERENCES comments (id),
    CONSTRAINT ck_comments_not_empty CHECK (char_length(trim(content)) > 0)
);

CREATE INDEX idx_comments_target_root_created
    ON comments (target_account_id, created_at DESC)
    WHERE parent_comment_id IS NULL;

CREATE INDEX idx_comments_parent_created
    ON comments (parent_comment_id, created_at ASC)
    WHERE parent_comment_id IS NOT NULL;

CREATE INDEX idx_comments_reply_target
    ON comments (reply_to_comment_id)
    WHERE reply_to_comment_id IS NOT NULL;

CREATE TABLE account_reactions
(
    actor_account_id  UUID        NOT NULL,
    target_account_id UUID        NOT NULL,
    type              VARCHAR(8)  NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL,

    PRIMARY KEY (actor_account_id, target_account_id),
    CONSTRAINT ck_account_reactions_type CHECK (type IN ('LIKE', 'DISLIKE')),
    CONSTRAINT ck_account_reactions_not_self CHECK (actor_account_id <> target_account_id)
);

CREATE INDEX idx_account_reactions_target
    ON account_reactions (target_account_id, type);

CREATE TABLE comment_likes
(
    comment_id       UUID        NOT NULL REFERENCES comments (id),
    actor_account_id UUID        NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL,

    PRIMARY KEY (comment_id, actor_account_id)
);

CREATE INDEX idx_comment_likes_comment
    ON comment_likes (comment_id);

CREATE TABLE account_social_summaries
(
    account_id UUID PRIMARY KEY,
    likes      BIGINT      NOT NULL DEFAULT 0,
    dislikes   BIGINT      NOT NULL DEFAULT 0,
    comments   BIGINT      NOT NULL DEFAULT 0,
    revision   BIGINT      NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT ck_account_social_summaries_counts
        CHECK (likes >= 0 AND dislikes >= 0 AND comments >= 0),
    CONSTRAINT ck_account_social_summaries_revision
        CHECK (revision >= 0)
);

CREATE TABLE spotlight_queue_state
(
    id            SMALLINT PRIMARY KEY,
    next_sequence BIGINT NOT NULL,

    CONSTRAINT ck_spotlight_queue_singleton CHECK (id = 1),
    CONSTRAINT ck_spotlight_queue_sequence CHECK (next_sequence > 0)
);

INSERT INTO spotlight_queue_state (id, next_sequence)
VALUES (1, 1);

CREATE TABLE drop_spotlights
(
    id                              UUID PRIMARY KEY,
    sequence                        BIGINT       NOT NULL UNIQUE,
    buyer_user_id                   UUID         NOT NULL,
    source_account_id               UUID         NOT NULL,
    item_drop_event_id              UUID         NOT NULL,
    item_id                         UUID         NOT NULL,
    item_name                       VARCHAR(128) NOT NULL,
    item_rarity                     VARCHAR(32)  NOT NULL,
    item_type                       VARCHAR(32)  NOT NULL,
    dropped_for                     VARCHAR(64)  NOT NULL,
    drop_occurred_at                TIMESTAMPTZ  NOT NULL,
    notification_pack_id            UUID,
    notification_pack_revision      BIGINT,
    notification_pack_snapshot_json TEXT,
    price_prime                     BIGINT       NOT NULL,
    wallet_request_id               UUID         NOT NULL UNIQUE,
    wallet_transaction_id           UUID,
    idempotency_key                 UUID,
    status                          VARCHAR(24)  NOT NULL,
    scheduled_at                    TIMESTAMPTZ  NOT NULL,
    published_at                    TIMESTAMPTZ,
    expires_at                      TIMESTAMPTZ,
    created_at                      TIMESTAMPTZ  NOT NULL,
    updated_at                      TIMESTAMPTZ  NOT NULL,

    CONSTRAINT ck_drop_spotlights_price CHECK (price_prime > 0),
    CONSTRAINT ck_drop_spotlights_status CHECK (
        status IN ('PENDING_PAYMENT', 'QUEUED', 'PUBLISHED', 'PAYMENT_FAILED')
    ),
    CONSTRAINT ck_drop_spotlights_pack_snapshot CHECK (
        (
            notification_pack_id IS NULL
                AND notification_pack_revision IS NULL
                AND notification_pack_snapshot_json IS NULL
        )
            OR
        (
            notification_pack_id IS NOT NULL
                AND notification_pack_revision IS NOT NULL
                AND notification_pack_snapshot_json IS NOT NULL
        )
    )
);

CREATE UNIQUE INDEX uq_drop_spotlights_item_drop_non_failed
    ON drop_spotlights (item_drop_event_id)
    WHERE status <> 'PAYMENT_FAILED';

CREATE UNIQUE INDEX uq_drop_spotlights_idempotency_key
    ON drop_spotlights (idempotency_key)
    WHERE idempotency_key IS NOT NULL;

CREATE INDEX idx_drop_spotlights_buyer_created
    ON drop_spotlights (buyer_user_id, created_at DESC);

CREATE INDEX idx_drop_spotlights_due
    ON drop_spotlights (status, scheduled_at);

CREATE INDEX idx_drop_spotlights_published
    ON drop_spotlights (status, published_at DESC);

CREATE TABLE outbox_events
(
    id             UUID PRIMARY KEY,
    sequence_no    BIGINT GENERATED ALWAYS AS IDENTITY,
    topic          VARCHAR(255) NOT NULL,
    event_key      VARCHAR(255) NOT NULL,
    event_type     VARCHAR(150) NOT NULL,
    event_version  INTEGER      NOT NULL,
    producer       VARCHAR(100) NOT NULL,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id   UUID         NOT NULL,
    correlation_id VARCHAR(255),
    causation_id   UUID,
    actor_user_id  UUID,
    envelope       JSONB        NOT NULL,
    occurred_at    TIMESTAMPTZ  NOT NULL,
    created_at     TIMESTAMPTZ  NOT NULL,
    status         VARCHAR(16)  NOT NULL DEFAULT 'PENDING',
    failure_count  INTEGER      NOT NULL DEFAULT 0,
    next_attempt_at TIMESTAMPTZ NOT NULL,
    locked_by      VARCHAR(255),
    locked_until   TIMESTAMPTZ,
    published_at   TIMESTAMPTZ,
    dead_at        TIMESTAMPTZ,
    last_error     TEXT,

    CONSTRAINT uq_outbox_events_sequence_no
        UNIQUE (sequence_no),
    CONSTRAINT ck_outbox_event_topic_not_blank
        CHECK (LENGTH(TRIM(topic)) > 0),
    CONSTRAINT ck_outbox_event_key_not_blank
        CHECK (LENGTH(TRIM(event_key)) > 0),
    CONSTRAINT ck_outbox_event_type_not_blank
        CHECK (LENGTH(TRIM(event_type)) > 0),
    CONSTRAINT ck_outbox_event_version_positive
        CHECK (event_version > 0),
    CONSTRAINT ck_outbox_producer_not_blank
        CHECK (LENGTH(TRIM(producer)) > 0),
    CONSTRAINT ck_outbox_aggregate_type_not_blank
        CHECK (LENGTH(TRIM(aggregate_type)) > 0),
    CONSTRAINT ck_outbox_failure_count_non_negative
        CHECK (failure_count >= 0),
    CONSTRAINT ck_outbox_status
        CHECK (status IN ('PENDING', 'PUBLISHED', 'DEAD')),
    CONSTRAINT ck_outbox_lock_consistency
        CHECK (
            (locked_by IS NULL AND locked_until IS NULL)
                OR
            (locked_by IS NOT NULL AND locked_until IS NOT NULL)
        ),
    CONSTRAINT ck_outbox_state_consistency
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
        ),
    CONSTRAINT ck_outbox_published_after_creation
        CHECK (published_at IS NULL OR published_at >= created_at),
    CONSTRAINT ck_outbox_dead_after_creation
        CHECK (dead_at IS NULL OR dead_at >= created_at)
);

CREATE INDEX idx_outbox_events_pending
    ON outbox_events (next_attempt_at, sequence_no)
    WHERE status = 'PENDING';

CREATE INDEX idx_outbox_events_pending_aggregate
    ON outbox_events (aggregate_type, aggregate_id, sequence_no)
    WHERE status = 'PENDING';

CREATE INDEX idx_outbox_events_published_cleanup
    ON outbox_events (published_at)
    WHERE status = 'PUBLISHED';

CREATE INDEX idx_outbox_events_dead_cleanup
    ON outbox_events (dead_at)
    WHERE status = 'DEAD';

CREATE TABLE inbox_events
(
    consumer    VARCHAR(150) NOT NULL,
    event_id    UUID         NOT NULL,
    recorded_at TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_inbox_events PRIMARY KEY (consumer, event_id),
    CONSTRAINT ck_inbox_consumer_not_blank CHECK (LENGTH(TRIM(consumer)) > 0)
);

CREATE INDEX idx_inbox_events_recorded_at
    ON inbox_events (recorded_at);
