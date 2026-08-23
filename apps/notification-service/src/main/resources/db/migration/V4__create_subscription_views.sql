CREATE VIEW subscription_views AS
SELECT
    s.id,
    s.user_id,
    s.type,
    s.created_at,
    item_drop.dropped_for,
    item_drop.item_name,
    booster.booster_type,
    dungeon.dungeon_type,
    global_subscription.event_type AS global_event_type,
    (
        SELECT string_agg(location.location::text, ',' ORDER BY location.location)
        FROM raid_subscription_locations location
        WHERE location.subscription_id = s.id
    ) AS raid_locations
FROM subscriptions s
LEFT JOIN item_drop_subscriptions item_drop
    ON item_drop.subscription_id = s.id
LEFT JOIN booster_subscriptions booster
    ON booster.subscription_id = s.id
LEFT JOIN dungeon_subscriptions dungeon
    ON dungeon.subscription_id = s.id
LEFT JOIN raid_subscriptions raid
    ON raid.subscription_id = s.id
LEFT JOIN global_subscriptions global_subscription
    ON global_subscription.subscription_id = s.id;
