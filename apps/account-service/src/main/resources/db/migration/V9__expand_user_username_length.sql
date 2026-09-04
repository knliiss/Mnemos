ALTER TABLE users
    DROP CONSTRAINT ck_users_username_length;

ALTER TABLE users
    ALTER COLUMN username TYPE VARCHAR(64);

ALTER TABLE users
    ADD CONSTRAINT ck_users_username_length
        CHECK (CHAR_LENGTH(BTRIM(username)) BETWEEN 3 AND 64);
