CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE TABLE public_profiles
(
    account_id          UUID PRIMARY KEY,
    cristalix_uuid      UUID         NOT NULL UNIQUE,
    cristalix_nickname  VARCHAR(64)  NOT NULL,
    likes               BIGINT       NOT NULL DEFAULT 0,
    dislikes            BIGINT       NOT NULL DEFAULT 0,
    comments            BIGINT       NOT NULL DEFAULT 0,
    social_revision     BIGINT       NOT NULL DEFAULT 0,
    created_at          TIMESTAMPTZ  NOT NULL,
    updated_at          TIMESTAMPTZ  NOT NULL,

    CONSTRAINT ck_public_profiles_social_counts
        CHECK (likes >= 0 AND dislikes >= 0 AND comments >= 0),
    CONSTRAINT ck_public_profiles_social_revision
        CHECK (social_revision >= 0)
);

CREATE INDEX idx_public_profiles_nickname_trgm
    ON public_profiles
        USING gin (cristalix_nickname gin_trgm_ops);

CREATE TABLE inbox_events
(
    event_id         UUID         NOT NULL,
    consumer         VARCHAR(200) NOT NULL,
    topic            VARCHAR(249) NOT NULL,
    partition_number INT          NOT NULL,
    offset_value     BIGINT       NOT NULL,
    event_type       VARCHAR(150) NOT NULL,
    event_version    INT          NOT NULL,
    aggregate_type   VARCHAR(150) NOT NULL,
    aggregate_id     UUID         NOT NULL,
    producer         VARCHAR(128) NOT NULL,
    occurred_at      TIMESTAMPTZ  NOT NULL,
    metadata_json    TEXT         NOT NULL,
    payload_json     TEXT         NOT NULL,
    status           VARCHAR(20)  NOT NULL,
    last_error       TEXT,
    received_at      TIMESTAMPTZ  NOT NULL,
    processed_at     TIMESTAMPTZ,
    updated_at       TIMESTAMPTZ  NOT NULL,

    CONSTRAINT pk_inbox_events
        PRIMARY KEY (event_id, consumer),
    CONSTRAINT ck_inbox_status
        CHECK (status IN ('PROCESSING', 'PROCESSED', 'FAILED')),
    CONSTRAINT ck_inbox_consumer_not_blank
        CHECK (BTRIM(consumer) <> ''),
    CONSTRAINT ck_inbox_topic_not_blank
        CHECK (BTRIM(topic) <> ''),
    CONSTRAINT ck_inbox_partition_non_negative
        CHECK (partition_number >= 0),
    CONSTRAINT ck_inbox_offset_non_negative
        CHECK (offset_value >= 0),
    CONSTRAINT ck_inbox_event_type_not_blank
        CHECK (BTRIM(event_type) <> ''),
    CONSTRAINT ck_inbox_event_version_positive
        CHECK (event_version > 0),
    CONSTRAINT ck_inbox_aggregate_type_not_blank
        CHECK (BTRIM(aggregate_type) <> ''),
    CONSTRAINT ck_inbox_producer_not_blank
        CHECK (BTRIM(producer) <> '')
);

CREATE INDEX idx_inbox_events_status_received_at
    ON inbox_events (status, received_at);

CREATE INDEX idx_inbox_events_consumer_status
    ON inbox_events (consumer, status);

CREATE INDEX idx_inbox_events_aggregate
    ON inbox_events (aggregate_type, aggregate_id, occurred_at);
