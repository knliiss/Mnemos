CREATE TABLE leaderboard_exclusions (
    id UUID PRIMARY KEY,
    subject_type VARCHAR(16) NOT NULL,
    subject_id UUID NOT NULL,
    scope VARCHAR(16) NOT NULL,
    reason VARCHAR(256),
    created_by_user_id UUID NOT NULL,
    created_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT leaderboard_exclusions_subject_type_check
        CHECK (subject_type IN ('ACCOUNT', 'PLAYER')),
    CONSTRAINT leaderboard_exclusions_scope_check
        CHECK (scope IN ('ALL', 'COINS', 'DROPS', 'BOOSTERS')),
    CONSTRAINT leaderboard_exclusions_subject_scope_unique
        UNIQUE (subject_type, subject_id, scope)
);

CREATE INDEX idx_leaderboard_exclusions_subject
    ON leaderboard_exclusions (subject_type, subject_id);
