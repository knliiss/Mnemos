CREATE TABLE player_tracking_state
(
    player_id             UUID PRIMARY KEY,
    active_until          TIMESTAMPTZ,
    next_poll_at          TIMESTAMPTZ NOT NULL,
    lease_until           TIMESTAMPTZ,
    last_poll_at          TIMESTAMPTZ,
    consecutive_failures  INTEGER     NOT NULL DEFAULT 0,
    created_at            TIMESTAMPTZ NOT NULL,
    updated_at            TIMESTAMPTZ NOT NULL,

    CONSTRAINT fk_player_tracking_state_player
        FOREIGN KEY (player_id)
            REFERENCES players (player_id)
            ON DELETE CASCADE,
    CONSTRAINT ck_player_tracking_state_failures
        CHECK (consecutive_failures >= 0)
);

CREATE INDEX idx_player_tracking_state_due
    ON player_tracking_state (next_poll_at, lease_until);
