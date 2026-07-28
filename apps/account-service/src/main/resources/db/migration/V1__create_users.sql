CREATE TABLE users
(
    id                    UUID PRIMARY KEY,
    keycloak_id           UUID        NOT NULL,
    telegram_id           BIGINT      NOT NULL,
    username              VARCHAR(20) NOT NULL,
    localization_language VARCHAR(16) NOT NULL,
    created_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,

    CONSTRAINT uk_users_keycloak_id
        UNIQUE (keycloak_id),

    CONSTRAINT uk_users_telegram_id
        UNIQUE (telegram_id),

    CONSTRAINT ck_users_telegram_id_positive
        CHECK (telegram_id > 0),

    CONSTRAINT ck_users_username_length
        CHECK (CHAR_LENGTH(BTRIM(username)) BETWEEN 3 AND 20)
);

CREATE UNIQUE INDEX uk_users_username_lower
    ON users (LOWER(username));