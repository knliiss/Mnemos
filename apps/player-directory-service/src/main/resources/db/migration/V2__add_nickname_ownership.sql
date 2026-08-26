ALTER TABLE players
    DROP CONSTRAINT players_normalized_nickname_key;

CREATE INDEX idx_players_normalized_nickname
    ON players (normalized_nickname);

CREATE TABLE player_nickname_owners
(
    normalized_nickname VARCHAR(64) PRIMARY KEY,
    nickname            VARCHAR(64) NOT NULL,
    player_id           UUID        NOT NULL UNIQUE,
    external_version    BIGINT      NOT NULL,
    observed_at         TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_player_nickname_owners_player
        FOREIGN KEY (player_id)
            REFERENCES players (player_id)
            ON DELETE CASCADE,
    CONSTRAINT ck_player_nickname_owners_external_version
        CHECK (external_version >= 0)
);

CREATE TABLE player_nickname_history
(
    id               UUID PRIMARY KEY,
    player_id        UUID        NOT NULL,
    nickname         VARCHAR(64) NOT NULL,
    normalized_nickname VARCHAR(64) NOT NULL,
    external_version BIGINT      NOT NULL,
    valid_from       TIMESTAMPTZ NOT NULL,
    valid_until      TIMESTAMPTZ,

    CONSTRAINT fk_player_nickname_history_player
        FOREIGN KEY (player_id)
            REFERENCES players (player_id)
            ON DELETE CASCADE,
    CONSTRAINT ck_player_nickname_history_external_version
        CHECK (external_version >= 0),
    CONSTRAINT ck_player_nickname_history_interval
        CHECK (valid_until IS NULL OR valid_until >= valid_from)
);

CREATE UNIQUE INDEX uq_player_nickname_history_open_player
    ON player_nickname_history (player_id)
    WHERE valid_until IS NULL;

CREATE INDEX idx_player_nickname_history_nickname_interval
    ON player_nickname_history (normalized_nickname, valid_from, valid_until);
