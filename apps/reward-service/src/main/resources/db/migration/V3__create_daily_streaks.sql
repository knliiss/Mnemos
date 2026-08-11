CREATE TABLE daily_streaks (
    account_id UUID PRIMARY KEY,
    current_streak INTEGER NOT NULL DEFAULT 0,
    last_claimed_date DATE,
    recoverable_streak INTEGER NOT NULL DEFAULT 0,
    restores_used INTEGER NOT NULL DEFAULT 0,
    restores_month VARCHAR(7) NOT NULL,
    CONSTRAINT ck_daily_streak_current CHECK (current_streak >= 0),
    CONSTRAINT ck_daily_streak_recoverable CHECK (recoverable_streak >= 0),
    CONSTRAINT ck_daily_streak_restores CHECK (restores_used >= 0)
);
CREATE INDEX idx_daily_streaks_last_claimed ON daily_streaks (last_claimed_date) WHERE current_streak > 0;
