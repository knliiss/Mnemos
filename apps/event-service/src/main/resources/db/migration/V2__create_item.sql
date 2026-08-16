CREATE EXTENSION IF NOT EXISTS pg_trgm WITH SCHEMA public;

CREATE TABLE items
(
    id     uuid PRIMARY KEY,
    name   varchar(24) NOT NULL,
    rarity varchar(16) NOT NULL,
    type   varchar(16) NOT NULL,

    CONSTRAINT ck_items_type
        CHECK (type IN (
                        'SWORD',
                        'AURA',
                        'PET',
                        'BOOK',
                        'JEWELRY'
            )),

    CONSTRAINT ck_items_rarity
        CHECK (rarity IN ('MYTHICAL', 'SECRET'))
);

CREATE INDEX idx_items_name_trgm
    ON items
        USING gin (name public.gin_trgm_ops);

CREATE UNIQUE INDEX uq_items_name_type
    ON items (LOWER(name), type);