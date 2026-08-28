CREATE TABLE cristalix_quota_states
(
    lane             VARCHAR(16)      PRIMARY KEY,
    available_tokens DOUBLE PRECISION NOT NULL,
    last_refill_at   TIMESTAMPTZ       NOT NULL,
    cooldown_until   TIMESTAMPTZ,

    CONSTRAINT ck_cristalix_quota_lane
        CHECK (lane IN ('INTERACTIVE', 'MONITORING', 'RESERVE')),
    CONSTRAINT ck_cristalix_quota_tokens
        CHECK (available_tokens >= 0)
);
