ALTER TABLE item_drop_events
    ADD COLUMN player_id UUID,
    ADD COLUMN identity_resolution_pending BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN identity_resolution_requested_at TIMESTAMPTZ;

ALTER TABLE item_drop_events
    ADD CONSTRAINT ck_item_drop_identity_resolution
        CHECK (
            (identity_resolution_pending = TRUE AND player_id IS NULL AND identity_resolution_requested_at IS NOT NULL)
            OR
            (identity_resolution_pending = FALSE AND identity_resolution_requested_at IS NULL)
        );

CREATE INDEX idx_item_drop_events_player_id
    ON item_drop_events (player_id, occurred_at DESC);

CREATE INDEX idx_item_drop_events_pending_identity
    ON item_drop_events (LOWER(dropped_for), identity_resolution_requested_at)
    WHERE identity_resolution_pending = TRUE;

ALTER TABLE booster_events
    ADD COLUMN player_id UUID,
    ADD COLUMN identity_resolution_pending BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN identity_resolution_requested_at TIMESTAMPTZ;

ALTER TABLE booster_events
    ADD CONSTRAINT ck_booster_identity_resolution
        CHECK (
            (identity_resolution_pending = TRUE AND player_id IS NULL AND identity_resolution_requested_at IS NOT NULL)
            OR
            (identity_resolution_pending = FALSE AND identity_resolution_requested_at IS NULL)
        );

CREATE INDEX idx_booster_events_player_id
    ON booster_events (player_id, activated_at DESC);

CREATE INDEX idx_booster_events_pending_identity
    ON booster_events (LOWER(activated_by), identity_resolution_requested_at)
    WHERE identity_resolution_pending = TRUE;
