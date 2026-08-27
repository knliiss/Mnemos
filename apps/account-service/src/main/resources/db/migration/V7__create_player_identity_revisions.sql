CREATE TABLE player_identity_revisions
(
    player_id        UUID        NOT NULL,
    nickname         VARCHAR(20) NOT NULL,
    external_version BIGINT      NOT NULL,
    observed_at      TIMESTAMPTZ NOT NULL,
    updated_at       TIMESTAMPTZ NOT NULL,

    CONSTRAINT pk_player_identity_revisions
        PRIMARY KEY (player_id),

    CONSTRAINT ck_player_identity_revisions_external_version
        CHECK (external_version >= 0),

    CONSTRAINT ck_player_identity_revisions_nickname_length
        CHECK (CHAR_LENGTH(nickname) BETWEEN 4 AND 20)
);
