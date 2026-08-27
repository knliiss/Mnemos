UPDATE players
SET realm = NULL,
    realm_type = NULL,
    privacy_hidden = TRUE,
    last_seen_online = NULL
WHERE presence_status = 'HIDDEN';
