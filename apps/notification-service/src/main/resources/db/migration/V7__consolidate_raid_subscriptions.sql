CREATE TEMP TABLE raid_subscription_merge
ON COMMIT DROP
AS
SELECT
    s.user_id,
    (array_agg(s.id ORDER BY s.created_at ASC, s.id ASC))[1] AS canonical_id,
    bool_or(
        NOT EXISTS (
            SELECT 1
            FROM raid_subscription_locations rsl
            WHERE rsl.subscription_id = s.id
        )
    ) AS all_locations
FROM subscriptions s
WHERE s.type = 'RAID'
GROUP BY s.user_id;

INSERT INTO raid_subscription_locations (
    subscription_id,
    location
)
SELECT DISTINCT
    merge.canonical_id,
    location.location
FROM raid_subscription_merge merge
JOIN subscriptions subscription
    ON subscription.user_id = merge.user_id
   AND subscription.type = 'RAID'
JOIN raid_subscription_locations location
    ON location.subscription_id = subscription.id
WHERE NOT merge.all_locations
ON CONFLICT (subscription_id, location) DO NOTHING;

DELETE FROM raid_subscription_locations location
USING raid_subscription_merge merge
WHERE merge.all_locations
  AND location.subscription_id = merge.canonical_id;

DELETE FROM subscriptions subscription
USING raid_subscription_merge merge
WHERE subscription.type = 'RAID'
  AND subscription.user_id = merge.user_id
  AND subscription.id <> merge.canonical_id;

UPDATE subscriptions subscription
SET equivalence_key = 'raid'
FROM raid_subscription_merge merge
WHERE subscription.id = merge.canonical_id;

CREATE UNIQUE INDEX uq_subscriptions_one_raid_per_user
    ON subscriptions (user_id)
    WHERE type = 'RAID';
