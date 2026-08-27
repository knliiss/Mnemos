CREATE TABLE player_identity_projection
(
    player_id            UUID         PRIMARY KEY,
    nickname             VARCHAR(64)  NOT NULL,
    normalized_nickname  VARCHAR(64)  NOT NULL,
    external_version     BIGINT       NOT NULL,
    observed_at          TIMESTAMPTZ  NOT NULL,

    CONSTRAINT ck_player_identity_external_version
        CHECK (external_version >= 0),
    CONSTRAINT ck_player_identity_nickname_not_blank
        CHECK (LENGTH(TRIM(nickname)) > 0),
    CONSTRAINT ck_player_identity_normalized_nickname_not_blank
        CHECK (LENGTH(TRIM(normalized_nickname)) > 0)
);

CREATE INDEX idx_player_identity_normalized_nickname
    ON player_identity_projection (normalized_nickname);

CREATE TABLE current_nickname_owners
(
    normalized_nickname  VARCHAR(64)  PRIMARY KEY,
    nickname             VARCHAR(64)  NOT NULL,
    player_id            UUID         NOT NULL,
    external_version     BIGINT       NOT NULL,
    observed_at          TIMESTAMPTZ  NOT NULL,

    CONSTRAINT ck_current_nickname_owner_external_version
        CHECK (external_version >= 0),
    CONSTRAINT ck_current_nickname_owner_nickname_not_blank
        CHECK (LENGTH(TRIM(nickname)) > 0)
);

CREATE INDEX idx_current_nickname_owners_player_id
    ON current_nickname_owners (player_id);
