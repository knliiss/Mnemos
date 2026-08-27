CREATE TABLE leaderboard_exclusion_projection (
    subject_type VARCHAR(16) NOT NULL,
    subject_id UUID NOT NULL,
    scope VARCHAR(16) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,
    PRIMARY KEY (subject_type, subject_id, scope),
    CONSTRAINT wallet_leaderboard_exclusion_subject_type_check
        CHECK (subject_type IN ('ACCOUNT', 'PLAYER')),
    CONSTRAINT wallet_leaderboard_exclusion_scope_check
        CHECK (scope IN ('ALL', 'COINS', 'DROPS', 'BOOSTERS'))
);

CREATE INDEX idx_wallet_leaderboard_exclusion_subject
    ON leaderboard_exclusion_projection (subject_type, subject_id, scope);
