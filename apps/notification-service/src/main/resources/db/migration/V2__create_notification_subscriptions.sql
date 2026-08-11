CREATE TABLE subscriptions
(
    id              uuid PRIMARY KEY,
    user_id         uuid        NOT NULL,
    type            varchar(32) NOT NULL,
    equivalence_key text        NOT NULL,
    created_at      timestamptz NOT NULL,

    CONSTRAINT ck_subscriptions_type
        CHECK (type IN (
                        'ITEM_DROP',
                        'RAID',
                        'DUNGEON',
                        'BOOSTER',
                        'GLOBAL'
            )),

    CONSTRAINT uq_subscriptions_id_type
        UNIQUE (id, type),

    CONSTRAINT uq_subscriptions_equivalence
        UNIQUE (user_id, type, equivalence_key)
);


CREATE TABLE item_drop_subscriptions
(
    subscription_id   uuid        PRIMARY KEY,
    subscription_type varchar(32) NOT NULL DEFAULT 'ITEM_DROP',
    dropped_for       varchar(20),
    item_name         varchar(24),

    CONSTRAINT ck_item_drop_subscriptions_type
        CHECK (subscription_type = 'ITEM_DROP'),

    CONSTRAINT fk_item_drop_subscriptions_subscription
        FOREIGN KEY (subscription_id, subscription_type)
            REFERENCES subscriptions (id, type)
            ON DELETE CASCADE
            DEFERRABLE INITIALLY DEFERRED,

    CONSTRAINT ck_item_drop_subscriptions_filter
        CHECK (
            dropped_for IS NOT NULL
                OR item_name IS NOT NULL
            ),

    CONSTRAINT ck_item_drop_subscriptions_dropped_for
        CHECK (
            dropped_for IS NULL
                OR (
                LENGTH(dropped_for) BETWEEN 4 AND 20
                    AND dropped_for !~ '\s'
                )
            ),

    CONSTRAINT ck_item_drop_subscriptions_item_name
        CHECK (
            item_name IS NULL
                OR LENGTH(item_name) BETWEEN 1 AND 24
            )
);

CREATE INDEX idx_item_drop_subscriptions_dropped_for
    ON item_drop_subscriptions (LOWER(dropped_for))
    WHERE dropped_for IS NOT NULL;

CREATE INDEX idx_item_drop_subscriptions_item_name
    ON item_drop_subscriptions (LOWER(item_name))
    WHERE item_name IS NOT NULL;


CREATE TABLE booster_subscriptions
(
    subscription_id   uuid        PRIMARY KEY,
    subscription_type varchar(32) NOT NULL DEFAULT 'BOOSTER',
    booster_type      varchar(16) NOT NULL,

    CONSTRAINT ck_booster_subscriptions_type
        CHECK (subscription_type = 'BOOSTER'),

    CONSTRAINT fk_booster_subscriptions_subscription
        FOREIGN KEY (subscription_id, subscription_type)
            REFERENCES subscriptions (id, type)
            ON DELETE CASCADE
            DEFERRABLE INITIALLY DEFERRED,

    CONSTRAINT ck_booster_subscriptions_booster_type
        CHECK (booster_type IN (
                                'LUCK',
                                'MONEY',
                                'DAMAGE',
                                'POWER'
            ))
);

CREATE INDEX idx_booster_subscriptions_booster_type
    ON booster_subscriptions (booster_type);


CREATE TABLE raid_subscriptions
(
    subscription_id   uuid        PRIMARY KEY,
    subscription_type varchar(32) NOT NULL DEFAULT 'RAID',

    CONSTRAINT ck_raid_subscriptions_type
        CHECK (subscription_type = 'RAID'),

    CONSTRAINT fk_raid_subscriptions_subscription
        FOREIGN KEY (subscription_id, subscription_type)
            REFERENCES subscriptions (id, type)
            ON DELETE CASCADE
            DEFERRABLE INITIALLY DEFERRED
);

CREATE TABLE raid_subscription_locations
(
    subscription_id uuid NOT NULL,
    location        int  NOT NULL,

    PRIMARY KEY (subscription_id, location),

    CONSTRAINT fk_raid_subscription_locations_subscription
        FOREIGN KEY (subscription_id)
            REFERENCES raid_subscriptions (subscription_id)
            ON DELETE CASCADE
);

CREATE INDEX idx_raid_subscription_locations_location
    ON raid_subscription_locations (location);


CREATE TABLE global_subscriptions
(
    subscription_id   uuid        PRIMARY KEY,
    subscription_type varchar(32) NOT NULL DEFAULT 'GLOBAL',
    event_type        varchar(16) NOT NULL,

    CONSTRAINT ck_global_subscriptions_type
        CHECK (subscription_type = 'GLOBAL'),

    CONSTRAINT fk_global_subscriptions_subscription
        FOREIGN KEY (subscription_id, subscription_type)
            REFERENCES subscriptions (id, type)
            ON DELETE CASCADE
            DEFERRABLE INITIALLY DEFERRED,

    CONSTRAINT ck_global_subscriptions_event_type
        CHECK (event_type IN (
                              'DARKNESS',
                              'MOON',
                              'ECLIPSE',
                              'EXPLOSION'
            ))
);

CREATE INDEX idx_global_subscriptions_event_type
    ON global_subscriptions (event_type);


CREATE TABLE dungeon_subscriptions
(
    subscription_id   uuid        PRIMARY KEY,
    subscription_type varchar(32) NOT NULL DEFAULT 'DUNGEON',
    dungeon_type      varchar(16) NOT NULL,

    CONSTRAINT ck_dungeon_subscriptions_type
        CHECK (subscription_type = 'DUNGEON'),

    CONSTRAINT fk_dungeon_subscriptions_subscription
        FOREIGN KEY (subscription_id, subscription_type)
            REFERENCES subscriptions (id, type)
            ON DELETE CASCADE
            DEFERRABLE INITIALLY DEFERRED,

    CONSTRAINT ck_dungeon_subscriptions_dungeon_type
        CHECK (dungeon_type IN (
                                'EASY',
                                'MEDIUM',
                                'HARD',
                                'EXPERT'
            ))
);

CREATE INDEX idx_dungeon_subscriptions_dungeon_type
    ON dungeon_subscriptions (dungeon_type);