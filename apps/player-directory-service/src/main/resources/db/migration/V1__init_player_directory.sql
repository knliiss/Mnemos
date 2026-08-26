CREATE TABLE players
(
    player_id               UUID PRIMARY KEY,
    current_nickname        VARCHAR(64)  NOT NULL,
    normalized_nickname     VARCHAR(64)  NOT NULL UNIQUE,
    external_version        BIGINT       NOT NULL,
    external_updated_at     TIMESTAMPTZ,
    presence_status         VARCHAR(16)  NOT NULL,
    realm                   VARCHAR(64),
    realm_type              VARCHAR(64),
    privacy_hidden          BOOLEAN      NOT NULL DEFAULT FALSE,
    last_seen_online        TIMESTAMPTZ,
    presence_observed_at    TIMESTAMPTZ  NOT NULL,
    created_at              TIMESTAMPTZ  NOT NULL,
    updated_at              TIMESTAMPTZ  NOT NULL,

    CONSTRAINT ck_players_external_version
        CHECK (external_version >= 0),
    CONSTRAINT ck_players_presence_status
        CHECK (presence_status IN ('ONLINE', 'OFFLINE', 'UNKNOWN', 'HIDDEN'))
);

CREATE INDEX idx_players_online_realm
    ON players (realm, presence_observed_at)
    WHERE presence_status = 'ONLINE';

CREATE TABLE presence_subscriptions
(
    id                  UUID PRIMARY KEY,
    subscriber_user_id  UUID        NOT NULL,
    player_id           UUID        NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_presence_subscriptions_player
        FOREIGN KEY (player_id)
            REFERENCES players (player_id)
            ON DELETE CASCADE,
    CONSTRAINT uq_presence_subscriptions_user_player
        UNIQUE (subscriber_user_id, player_id)
);

CREATE INDEX idx_presence_subscriptions_player
    ON presence_subscriptions (player_id);
