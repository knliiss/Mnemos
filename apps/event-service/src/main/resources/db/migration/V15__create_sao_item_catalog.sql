CREATE TABLE sao_catalog_items (
    game_key varchar(160) PRIMARY KEY,
    default_name varchar(128) NOT NULL,
    item_type varchar(32) NOT NULL,
    rarity varchar(32),
    active boolean NOT NULL,
    source_hash varchar(64) NOT NULL,
    updated_at timestamptz NOT NULL
);

CREATE INDEX idx_sao_catalog_items_default_name_lower
    ON sao_catalog_items (LOWER(default_name));

CREATE INDEX idx_sao_catalog_items_type_rarity
    ON sao_catalog_items (item_type, rarity);

CREATE INDEX idx_sao_catalog_items_active
    ON sao_catalog_items (active);

CREATE TABLE sao_catalog_item_locations (
    game_key varchar(160) NOT NULL,
    location_id integer NOT NULL,

    PRIMARY KEY (game_key, location_id),

    CONSTRAINT fk_sao_catalog_item_locations_item
        FOREIGN KEY (game_key)
            REFERENCES sao_catalog_items (game_key)
            ON DELETE CASCADE
);

CREATE INDEX idx_sao_catalog_item_locations_location
    ON sao_catalog_item_locations (location_id, game_key);

CREATE TABLE item_canonical_identities (
    item_id uuid PRIMARY KEY,
    game_key varchar(160) NOT NULL,
    resolved_at timestamptz NOT NULL,

    CONSTRAINT fk_item_canonical_identities_item
        FOREIGN KEY (item_id)
            REFERENCES items (id)
            ON DELETE CASCADE,

    CONSTRAINT fk_item_canonical_identities_catalog_item
        FOREIGN KEY (game_key)
            REFERENCES sao_catalog_items (game_key)
            ON DELETE RESTRICT
);

CREATE INDEX idx_item_canonical_identities_game_key
    ON item_canonical_identities (game_key);
