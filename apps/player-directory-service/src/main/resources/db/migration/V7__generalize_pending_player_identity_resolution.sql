ALTER TABLE pending_player_activity
    ALTER COLUMN last_drop_at DROP NOT NULL;

ALTER TABLE pending_player_activity
    DROP CONSTRAINT ck_pending_player_activity_drop_count;

ALTER TABLE pending_player_activity
    ADD CONSTRAINT ck_pending_player_activity_drop_count
        CHECK (pending_drop_count >= 0);

ALTER TABLE pending_player_activity
    ADD CONSTRAINT ck_pending_player_activity_drop_state
        CHECK (
            (pending_drop_count = 0 AND last_drop_at IS NULL)
            OR
            (pending_drop_count > 0 AND last_drop_at IS NOT NULL)
        );
