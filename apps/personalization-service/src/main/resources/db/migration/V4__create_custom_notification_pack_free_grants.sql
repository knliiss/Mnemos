CREATE TABLE custom_notification_pack_free_grants
(
    user_id    UUID PRIMARY KEY,
    claimed_at TIMESTAMPTZ NOT NULL
);

INSERT INTO custom_notification_pack_free_grants (user_id, claimed_at)
SELECT creator_user_id, MIN(created_at)
FROM personalizations
WHERE origin = 'CUSTOM'
  AND type = 'NOTIFICATION_PACK'
GROUP BY creator_user_id;
