CREATE TABLE telegram_comment_receipts
(
    operation_id     UUID PRIMARY KEY,
    telegram_id      BIGINT       NOT NULL,
    actor_user_id    UUID         NOT NULL,
    actor_account_id UUID         NOT NULL,
    target_account_id UUID        NOT NULL,
    request_hash     CHAR(64)     NOT NULL,
    comment_id       UUID,
    created_at       TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_telegram_comment_receipts_created
    ON telegram_comment_receipts (created_at);
