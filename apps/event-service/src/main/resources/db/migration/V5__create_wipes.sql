CREATE TABLE wipes
(
    id         uuid PRIMARY KEY,
    number     integer      NOT NULL,
    name       varchar(64),
    started_at timestamptz  NOT NULL,
    ended_at   timestamptz,
    created_by varchar(128) NOT NULL,
    created_at timestamptz  NOT NULL,

    CONSTRAINT uq_wipes_number UNIQUE (number),
    CONSTRAINT ck_wipes_number CHECK (number > 0),
    CONSTRAINT ck_wipes_dates CHECK (ended_at IS NULL OR ended_at > started_at)
);

CREATE UNIQUE INDEX uq_wipes_active
    ON wipes ((ended_at IS NULL))
    WHERE ended_at IS NULL;

CREATE INDEX idx_wipes_started_at
    ON wipes (started_at DESC);

INSERT INTO wipes (
    id,
    number,
    name,
    started_at,
    ended_at,
    created_by,
    created_at
)
VALUES (
    '00000000-0000-7000-8000-000000000001',
    1,
    'Legacy',
    '1970-01-01T00:00:00Z',
    NULL,
    'system:migration',
    now()
);
