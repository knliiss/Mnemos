CREATE INDEX idx_owned_personalizations_owner_type
    ON owned_personalizations (
        owner_type,
        owner_id,
        personalization_type,
        acquired_at DESC,
        id DESC
    );
