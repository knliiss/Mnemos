CREATE TABLE comments
(
    id                UUID PRIMARY KEY,
    target_account_id UUID         NOT NULL,
    author_user_id    UUID         NOT NULL,
    author_account_id UUID         NOT NULL,
    parent_comment_id UUID,
    content           VARCHAR(500) NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL,
    deleted_at        TIMESTAMPTZ,

    CONSTRAINT fk_comments_parent FOREIGN KEY (parent_comment_id) REFERENCES comments (id),
    CONSTRAINT ck_comments_not_empty CHECK (char_length(trim(content)) > 0)
);

CREATE INDEX idx_comments_target_root_created
    ON comments (target_account_id, created_at DESC)
    WHERE parent_comment_id IS NULL;

CREATE INDEX idx_comments_parent_created
    ON comments (parent_comment_id, created_at ASC)
    WHERE parent_comment_id IS NOT NULL;

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

CREATE INDEX idx_comment_likes_comment ON comment_likes (comment_id);

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
    id                                UUID PRIMARY KEY,
    sequence                          BIGINT       NOT NULL UNIQUE,
    buyer_user_id                     UUID         NOT NULL,
    source_account_id                 UUID         NOT NULL,
    item_drop_event_id                UUID         NOT NULL,
    item_id                           UUID         NOT NULL,
    item_name                         VARCHAR(128) NOT NULL,
    item_rarity                       VARCHAR(32)  NOT NULL,
    item_type                         VARCHAR(32)  NOT NULL,
    dropped_for                       VARCHAR(64)  NOT NULL,
    drop_occurred_at                  TIMESTAMPTZ  NOT NULL,
    notification_pack_id              UUID,
    notification_pack_revision        BIGINT,
    notification_pack_snapshot_json   TEXT,
    price_prime                       BIGINT       NOT NULL,
    wallet_request_id                 UUID         NOT NULL UNIQUE,
    wallet_transaction_id             UUID,
    status                            VARCHAR(24)  NOT NULL,
    scheduled_at                      TIMESTAMPTZ  NOT NULL,
    published_at                      TIMESTAMPTZ,
    expires_at                        TIMESTAMPTZ,
    created_at                        TIMESTAMPTZ  NOT NULL,
    updated_at                        TIMESTAMPTZ  NOT NULL,

    CONSTRAINT ck_drop_spotlights_price CHECK (price_prime > 0),
    CONSTRAINT ck_drop_spotlights_status CHECK (status IN ('PENDING_PAYMENT', 'QUEUED', 'PUBLISHED', 'PAYMENT_FAILED')),
    CONSTRAINT ck_drop_spotlights_pack_snapshot CHECK (
        (notification_pack_id IS NULL AND notification_pack_revision IS NULL AND notification_pack_snapshot_json IS NULL)
        OR (notification_pack_id IS NOT NULL AND notification_pack_revision IS NOT NULL AND notification_pack_snapshot_json IS NOT NULL)
    )
);

CREATE UNIQUE INDEX uq_drop_spotlights_item_drop_non_failed
    ON drop_spotlights (item_drop_event_id)
    WHERE status <> 'PAYMENT_FAILED';

CREATE INDEX idx_drop_spotlights_buyer_created
    ON drop_spotlights (buyer_user_id, created_at DESC);

CREATE INDEX idx_drop_spotlights_due
    ON drop_spotlights (status, scheduled_at);

CREATE INDEX idx_drop_spotlights_published
    ON drop_spotlights (status, published_at DESC);
