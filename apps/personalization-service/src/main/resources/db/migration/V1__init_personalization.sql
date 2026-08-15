CREATE TABLE personalizations
(
    id               UUID PRIMARY KEY,
    creator_user_id  UUID         NOT NULL,
    type             VARCHAR(32)  NOT NULL,
    origin           VARCHAR(16)  NOT NULL,
    visibility       VARCHAR(16)  NOT NULL,
    name             VARCHAR(64)  NOT NULL,
    content          JSONB        NOT NULL,
    price_currency   VARCHAR(16),
    price_amount     BIGINT,
    status           VARCHAR(16)  NOT NULL,
    rejection_reason VARCHAR(500),
    created_at       TIMESTAMPTZ  NOT NULL,
    updated_at       TIMESTAMPTZ  NOT NULL,

    CONSTRAINT ck_personalizations_type
        CHECK (type IN (
            'NOTIFICATION_PACK',
            'NICK_STYLE',
            'PROFILE_THEME',
            'AVATAR_FRAME',
            'PROFILE_EFFECT'
        )),
    CONSTRAINT ck_personalizations_origin
        CHECK (origin IN ('CREATOR', 'CUSTOM')),
    CONSTRAINT ck_personalizations_visibility
        CHECK (visibility IN ('PRIVATE', 'PUBLIC')),
    CONSTRAINT ck_personalizations_status
        CHECK (status IN (
            'DRAFT',
            'PRIVATE',
            'IN_REVIEW',
            'REJECTED',
            'APPROVED',
            'PUBLISHED',
            'UNLISTED'
        )),
    CONSTRAINT ck_personalizations_price_pair
        CHECK (
            (price_currency IS NULL AND price_amount IS NULL)
            OR
            (price_currency IS NOT NULL AND price_amount IS NOT NULL)
        ),
    CONSTRAINT ck_personalizations_price_currency
        CHECK (price_currency IS NULL OR price_currency IN ('COIN', 'PRIME')),
    CONSTRAINT ck_personalizations_price_amount
        CHECK (price_amount IS NULL OR price_amount > 0),
    CONSTRAINT ck_notification_pack_prime_only
        CHECK (
            type <> 'NOTIFICATION_PACK'
            OR price_currency IS NULL
            OR price_currency = 'PRIME'
        )
);

CREATE INDEX idx_personalizations_creator_created_at
    ON personalizations (creator_user_id, created_at DESC);

CREATE INDEX idx_personalizations_status_created_at
    ON personalizations (status, created_at ASC);

CREATE INDEX idx_personalizations_marketplace
    ON personalizations (type, price_currency, created_at DESC)
    WHERE status = 'PUBLISHED' AND visibility = 'PUBLIC';

CREATE TABLE owned_personalizations
(
    id                   UUID PRIMARY KEY,
    owner_type           VARCHAR(16) NOT NULL,
    owner_id             UUID        NOT NULL,
    personalization_id   UUID        NOT NULL REFERENCES personalizations(id),
    personalization_type VARCHAR(32) NOT NULL,
    acquired_at          TIMESTAMPTZ NOT NULL,
    active               BOOLEAN     NOT NULL DEFAULT FALSE,

    CONSTRAINT uq_owned_personalizations_owner_item
        UNIQUE (owner_type, owner_id, personalization_id),
    CONSTRAINT ck_owned_personalizations_owner_type
        CHECK (owner_type IN ('USER', 'ACCOUNT')),
    CONSTRAINT ck_owned_personalizations_type
        CHECK (personalization_type IN (
            'NOTIFICATION_PACK',
            'NICK_STYLE',
            'PROFILE_THEME',
            'AVATAR_FRAME',
            'PROFILE_EFFECT'
        ))
);

CREATE INDEX idx_owned_personalizations_owner
    ON owned_personalizations (owner_type, owner_id, acquired_at DESC);

CREATE UNIQUE INDEX uq_owned_personalizations_active_slot
    ON owned_personalizations (owner_type, owner_id, personalization_type)
    WHERE active = TRUE;

CREATE TABLE badges
(
    id         UUID PRIMARY KEY,
    code       VARCHAR(32)  NOT NULL UNIQUE,
    name       VARCHAR(64)  NOT NULL,
    icon       VARCHAR(64)  NOT NULL,
    color      VARCHAR(7)   NOT NULL,
    created_at TIMESTAMPTZ  NOT NULL,

    CONSTRAINT ck_badges_color
        CHECK (color ~ '^#[0-9A-Fa-f]{6}$')
);

CREATE TABLE user_badges
(
    id         UUID PRIMARY KEY,
    user_id    UUID        NOT NULL,
    badge_id   UUID        NOT NULL REFERENCES badges(id),
    granted_at TIMESTAMPTZ NOT NULL,
    active     BOOLEAN     NOT NULL DEFAULT FALSE,

    CONSTRAINT uq_user_badges_user_badge
        UNIQUE (user_id, badge_id)
);

CREATE INDEX idx_user_badges_user
    ON user_badges (user_id, granted_at DESC);

CREATE UNIQUE INDEX uq_user_badges_active
    ON user_badges (user_id)
    WHERE active = TRUE;

CREATE TABLE purchase_operations
(
    id                    UUID PRIMARY KEY,
    request_id            UUID        NOT NULL UNIQUE,
    personalization_id    UUID        NOT NULL REFERENCES personalizations(id),
    buyer_user_id         UUID        NOT NULL,
    owner_type            VARCHAR(16) NOT NULL,
    owner_id              UUID        NOT NULL,
    currency              VARCHAR(16) NOT NULL,
    amount                BIGINT      NOT NULL,
    status                VARCHAR(16) NOT NULL,
    wallet_transaction_id UUID,
    failure_reason        VARCHAR(128),
    created_at            TIMESTAMPTZ NOT NULL,

    CONSTRAINT ck_purchase_operations_owner_type
        CHECK (owner_type IN ('USER', 'ACCOUNT')),
    CONSTRAINT ck_purchase_operations_currency
        CHECK (currency IN ('COIN', 'PRIME')),
    CONSTRAINT ck_purchase_operations_amount
        CHECK (amount > 0),
    CONSTRAINT ck_purchase_operations_status
        CHECK (status IN ('PENDING', 'CONFIRMED', 'FAILED'))
);

CREATE INDEX idx_purchase_operations_buyer_created_at
    ON purchase_operations (buyer_user_id, created_at DESC);

CREATE INDEX idx_purchase_operations_personalization
    ON purchase_operations (personalization_id, created_at DESC);
