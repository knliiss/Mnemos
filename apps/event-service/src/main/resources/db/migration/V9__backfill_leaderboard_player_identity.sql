UPDATE item_drop_events AS event
SET player_id = owner.player_id
FROM current_nickname_owners AS owner
WHERE event.player_id IS NULL
  AND event.identity_resolution_pending = FALSE
  AND LOWER(TRIM(event.dropped_for)) = owner.normalized_nickname;

UPDATE booster_events AS event
SET player_id = owner.player_id
FROM current_nickname_owners AS owner
WHERE event.player_id IS NULL
  AND event.identity_resolution_pending = FALSE
  AND LOWER(TRIM(event.activated_by)) = owner.normalized_nickname;
