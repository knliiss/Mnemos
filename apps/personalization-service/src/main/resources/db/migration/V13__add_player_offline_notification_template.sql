UPDATE personalizations
SET content = jsonb_set(
    content,
    '{templates,PLAYER_OFFLINE}',
    '{"emoji":"🌙","soloTitle":"Игрок вышел из игры","soloBody":"{player} больше не в сети.","compact":"{player} вышел из игры","aggregateTitle":"Игроки вышли из игры","variants":[]}'::jsonb,
    true
)
WHERE type = 'NOTIFICATION_PACK'
  AND NOT (content -> 'templates' ? 'PLAYER_OFFLINE');
