ALTER TABLE player_tracking_state
    ADD COLUMN observed_drop_count BIGINT NOT NULL DEFAULT 0,
    ADD COLUMN last_drop_at TIMESTAMPTZ,
    ADD COLUMN last_sao_online_at TIMESTAMPTZ;

ALTER TABLE player_tracking_state
    ADD CONSTRAINT ck_player_tracking_state_observed_drop_count
        CHECK (observed_drop_count >= 0);

CREATE INDEX idx_player_tracking_state_last_drop_at
    ON player_tracking_state (last_drop_at)
    WHERE last_drop_at IS NOT NULL;

CREATE INDEX idx_player_tracking_state_last_sao_online_at
    ON player_tracking_state (last_sao_online_at)
    WHERE last_sao_online_at IS NOT NULL;
