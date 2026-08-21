CREATE TABLE recipients
(
    user_id          UUID PRIMARY KEY,
    telegram_id      BIGINT      NOT NULL,
    priority         VARCHAR(16) NOT NULL DEFAULT 'STANDARD',
    delivery_status  VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',

    CONSTRAINT uq_recipients_telegram_id
        UNIQUE (telegram_id),
    CONSTRAINT ck_recipients_priority
        CHECK (priority IN ('STANDARD', 'ELEVATED', 'PRIORITY')),
    CONSTRAINT ck_recipients_delivery_status
        CHECK (delivery_status IN ('ACTIVE', 'BLOCKED'))
);
