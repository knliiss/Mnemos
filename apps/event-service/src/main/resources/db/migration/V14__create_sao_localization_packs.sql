CREATE TABLE sao_localization_packs
(
    locale      varchar(16) PRIMARY KEY,
    hash        varchar(64)  NOT NULL,
    properties  jsonb        NOT NULL,
    updated_at  timestamptz  NOT NULL
);

CREATE INDEX idx_sao_localization_packs_updated_at
    ON sao_localization_packs (updated_at DESC);
