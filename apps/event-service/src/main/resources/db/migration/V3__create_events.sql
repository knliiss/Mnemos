CREATE TABLE item_drop_events
(
    id          uuid PRIMARY KEY,
    item_id     uuid        NOT NULL,
    dropped_for varchar(16) NOT NULL,
    occurred_at timestamptz NOT NULL,

    CONSTRAINT fk_item_drop_events_item_id
        FOREIGN KEY (item_id)
            REFERENCES items (id)
            ON DELETE RESTRICT
);

CREATE INDEX idx_item_drop_events_dropped_for_occurred_at
    ON item_drop_events (dropped_for, occurred_at DESC);

CREATE INDEX idx_item_drop_events_item_occurred_at
    ON item_drop_events (item_id, occurred_at DESC);

CREATE TABLE booster_events
(
    id           uuid PRIMARY KEY,
    type         varchar(16) NOT NULL,
    activated_by varchar(16) NOT NULL,
    activated_at timestamptz NOT NULL
);

CREATE INDEX idx_booster_events_activated_by_activated_at
    ON booster_events (activated_by, activated_at DESC);
CREATE INDEX idx_booster_events_type_activated_at
    ON booster_events (type, activated_at DESC);
CREATE INDEX idx_booster_events_activated_at
    ON booster_events (activated_at DESC);


CREATE TABLE raid_events
(
    id          uuid PRIMARY KEY,
    started_at timestamptz NOT NULL
);

CREATE TABLE raid_event_locations
(
    raid_event_id uuid NOT NULL,
    location      INT  NOT NULL,

    PRIMARY KEY (raid_event_id, location),

    CONSTRAINT fk_raid_event_locations_raid_event_id
        FOREIGN KEY (raid_event_id)
            REFERENCES raid_events (id)
);

CREATE INDEX idx_raid_events_occurred_at
    ON raid_events (started_at DESC);

CREATE INDEX idx_raid_event_locations_location
    ON raid_event_locations (location);

CREATE TABLE global_events
(
    id          uuid PRIMARY KEY,
    type        varchar(16) NOT NULL,
    started_at timestamptz NOT NULL,

    CONSTRAINT ck_global_events_type
        CHECK (type IN
               (
                'DARKNESS',
                'MOON',
                'ECLIPSE',
                'EXPLOSION'
                   )
            )
);

CREATE INDEX idx_global_events_occurred_at
    ON global_events (started_at DESC);

CREATE INDEX idx_global_events_type_occurred_at
    ON global_events (type, started_at DESC);