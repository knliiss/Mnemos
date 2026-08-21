CREATE TABLE spotlight_queue_state
(
    id            SMALLINT PRIMARY KEY,
    next_sequence BIGINT NOT NULL,

    CONSTRAINT ck_spotlight_queue_singleton
        CHECK (id = 1),

    CONSTRAINT ck_spotlight_queue_sequence
        CHECK (next_sequence > 0)
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

    CONSTRAINT ck_drop_spotlights_price
        CHECK (price_prime > 0),

    CONSTRAINT ck_drop_spotlights_status
        CHECK (status IN ('PENDING_PAYMENT', 'QUEUED', 'PUBLISHED', 'PAYMENT_FAILED')),

    CONSTRAINT ck_drop_spotlights_pack_snapshot
        CHECK (
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
