CREATE TABLE profile_player_states
(
    player_id             UUID PRIMARY KEY,
    nickname              VARCHAR(64) NOT NULL,
    external_version      BIGINT      NOT NULL,
    presence_status       VARCHAR(16) NOT NULL,
    realm                 VARCHAR(64),
    realm_type            VARCHAR(64),
    privacy_hidden        BOOLEAN     NOT NULL,
    last_seen_online      TIMESTAMPTZ,
    presence_observed_at  TIMESTAMPTZ NOT NULL,
    updated_at            TIMESTAMPTZ NOT NULL,

    CONSTRAINT ck_profile_player_state_external_version
        CHECK (external_version >= 0),
    CONSTRAINT ck_profile_player_state_presence_status
        CHECK (presence_status IN ('ONLINE', 'OFFLINE', 'UNKNOWN', 'HIDDEN'))
);

CREATE INDEX idx_profile_player_states_nickname
    ON profile_player_states (LOWER(nickname));

CREATE INDEX idx_profile_player_states_presence
    ON profile_player_states (presence_status, realm, presence_observed_at);
