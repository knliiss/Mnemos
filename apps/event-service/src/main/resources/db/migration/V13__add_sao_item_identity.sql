ALTER TABLE items
    ALTER COLUMN name TYPE varchar(96),
    ADD COLUMN game_key varchar(160);

CREATE UNIQUE INDEX uq_items_game_key
    ON items (game_key)
    WHERE game_key IS NOT NULL;
