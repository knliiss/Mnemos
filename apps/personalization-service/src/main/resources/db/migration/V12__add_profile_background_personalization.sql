ALTER TABLE personalizations
    DROP CONSTRAINT ck_personalizations_type;

ALTER TABLE personalizations
    ADD CONSTRAINT ck_personalizations_type
        CHECK (type IN (
            'NOTIFICATION_PACK',
            'NICK_STYLE',
            'PROFILE_THEME',
            'PROFILE_BACKGROUND',
            'AVATAR_FRAME',
            'PROFILE_EFFECT'
        ));

ALTER TABLE owned_personalizations
    DROP CONSTRAINT ck_owned_personalizations_type;

ALTER TABLE owned_personalizations
    ADD CONSTRAINT ck_owned_personalizations_type
        CHECK (personalization_type IN (
            'NOTIFICATION_PACK',
            'NICK_STYLE',
            'PROFILE_THEME',
            'PROFILE_BACKGROUND',
            'AVATAR_FRAME',
            'PROFILE_EFFECT'
        ));
