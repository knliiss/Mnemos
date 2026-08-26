CREATE TABLE player_presence_subscriptions
(
    subscription_id     UUID PRIMARY KEY,
    subscriber_user_id  UUID        NOT NULL,
    player_id           UUID        NOT NULL,
    created_at          TIMESTAMPTZ NOT NULL,

    CONSTRAINT uq_player_presence_subscription_recipient
        UNIQUE (subscriber_user_id, player_id)
);

CREATE INDEX idx_player_presence_subscriptions_player
    ON player_presence_subscriptions (player_id, subscriber_user_id);
