CREATE INDEX idx_personalizations_custom_creator_created_at
    ON personalizations (
        creator_user_id,
        created_at DESC,
        id DESC
    )
    WHERE origin = 'CUSTOM' AND type = 'NOTIFICATION_PACK';
