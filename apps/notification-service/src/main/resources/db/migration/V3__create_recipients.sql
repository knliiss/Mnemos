CREATE TABLE recipients
(
    user_id     uuid PRIMARY KEY,
    telegram_id bigint NOT NULL,
    CONSTRAINT uq_recipients_telegram_id
        UNIQUE (telegram_id)
)