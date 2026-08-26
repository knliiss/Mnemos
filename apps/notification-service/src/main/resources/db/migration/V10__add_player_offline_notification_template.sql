UPDATE active_notification_packs
SET templates = jsonb_set(
    templates,
    '{PLAYER_OFFLINE}',
    '{"emoji":"🌙","soloTitle":"Игрок вышел из игры","soloBody":"{player} больше не в сети.","compact":"{player} вышел из игры","aggregateTitle":"Игроки вышли из игры","variants":[]}'::jsonb,
    true
)
WHERE personalization_id IS NOT NULL
  AND templates IS NOT NULL
  AND NOT (templates ? 'PLAYER_OFFLINE');
