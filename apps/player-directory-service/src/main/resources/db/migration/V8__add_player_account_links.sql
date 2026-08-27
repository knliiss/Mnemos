CREATE TABLE player_account_links
(
    player_id   UUID        PRIMARY KEY,
    account_id  UUID        NOT NULL UNIQUE,
    recorded_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_player_account_links_account_id
    ON player_account_links (account_id);
