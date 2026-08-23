CREATE TABLE event_bet_rounds
(
    id UUID PRIMARY KEY,
    event_type VARCHAR(16) NOT NULL,
    scheduled_at TIMESTAMPTZ NOT NULL,
    closes_at TIMESTAMPTZ NOT NULL,
    resolves_at TIMESTAMPTZ NOT NULL,
    yes_odds NUMERIC(8, 4) NOT NULL,
    no_odds NUMERIC(8, 4) NOT NULL,
    status VARCHAR(40) NOT NULL,
    effective_outcome VARCHAR(16),

    CONSTRAINT uq_event_bet_round_schedule
        UNIQUE (event_type, scheduled_at),
    CONSTRAINT ck_event_bet_round_event_type
        CHECK (event_type IN ('MOON', 'ECLIPSE', 'EXPLOSION')),
    CONSTRAINT ck_event_bet_round_status
        CHECK (status IN ('OPEN', 'LOCKED', 'AWAITING_RESULT', 'AWAITING_ADMIN_CONFIRMATION', 'SETTLED')),
    CONSTRAINT ck_event_bet_round_outcome
        CHECK (effective_outcome IS NULL OR effective_outcome IN ('YES', 'NO', 'REFUND')),
    CONSTRAINT ck_event_bet_round_odds
        CHECK (yes_odds > 1 AND no_odds > 1),
    CONSTRAINT ck_event_bet_round_dates
        CHECK (closes_at < scheduled_at AND resolves_at > scheduled_at)
);

CREATE INDEX idx_event_bet_rounds_synchronization
    ON event_bet_rounds (scheduled_at, resolves_at)
    WHERE status <> 'SETTLED';

CREATE TABLE event_bets
(
    id UUID PRIMARY KEY,
    round_id UUID NOT NULL REFERENCES event_bet_rounds (id) ON DELETE CASCADE,
    account_id UUID NOT NULL,
    side VARCHAR(4) NOT NULL,
    confirmed_amount BIGINT NOT NULL DEFAULT 0,
    pending_amount BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(32) NOT NULL,

    CONSTRAINT uq_event_bets_round_account
        UNIQUE (round_id, account_id),
    CONSTRAINT ck_event_bets_side
        CHECK (side IN ('YES', 'NO')),
    CONSTRAINT ck_event_bets_status
        CHECK (status IN (
            'ACTIVE',
            'SETTLEMENT_PENDING',
            'SETTLEMENT_FAILED',
            'SETTLED',
            'REFUND_PENDING',
            'REFUND_FAILED',
            'REFUNDED'
        )),
    CONSTRAINT ck_event_bets_amounts
        CHECK (confirmed_amount >= 0 AND pending_amount >= 0),
    CONSTRAINT ck_event_bets_total
        CHECK (confirmed_amount + pending_amount <= 5000)
);

CREATE INDEX idx_event_bets_account
    ON event_bets (account_id, id DESC);

CREATE TABLE event_bet_round_resolutions
(
    id UUID PRIMARY KEY,
    round_id UUID NOT NULL REFERENCES event_bet_rounds (id) ON DELETE CASCADE,
    outcome VARCHAR(16) NOT NULL,
    source VARCHAR(24) NOT NULL,
    resolved_by_user_id UUID,
    reason VARCHAR(1000),
    resolved_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT ck_event_bet_resolution_outcome
        CHECK (outcome IN ('YES', 'NO', 'REFUND')),
    CONSTRAINT ck_event_bet_resolution_source
        CHECK (source IN ('AUTOMATIC', 'ADMINISTRATIVE')),
    CONSTRAINT ck_event_bet_resolution_actor
        CHECK (
            (source = 'AUTOMATIC' AND resolved_by_user_id IS NULL AND reason IS NULL)
            OR
            (source = 'ADMINISTRATIVE' AND resolved_by_user_id IS NOT NULL AND reason IS NOT NULL)
        )
);

CREATE INDEX idx_event_bet_resolutions_round
    ON event_bet_round_resolutions (round_id, resolved_at ASC);

CREATE TABLE item_races
(
    id UUID PRIMARY KEY,
    item_name VARCHAR(24) NOT NULL,
    item_type VARCHAR(16) NOT NULL,
    registration_closes_at TIMESTAMPTZ NOT NULL,
    starts_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    entry_amount BIGINT NOT NULL,
    fee_percent INT NOT NULL,
    status VARCHAR(20) NOT NULL,
    winner_account_id UUID,

    CONSTRAINT ck_item_races_item_type
        CHECK (item_type IN ('SWORD', 'AURA', 'PET', 'BOOK', 'JEWELRY')),
    CONSTRAINT ck_item_races_entry_amount
        CHECK (entry_amount BETWEEN 1000 AND 5000),
    CONSTRAINT ck_item_races_fee
        CHECK (fee_percent = 5),
    CONSTRAINT ck_item_races_status
        CHECK (status IN ('REGISTRATION', 'ACTIVE', 'SETTLING', 'SETTLED', 'REFUNDING', 'REFUNDED', 'CANCELLED')),
    CONSTRAINT ck_item_races_dates
        CHECK (registration_closes_at <= starts_at AND expires_at > starts_at)
);

CREATE INDEX idx_item_races_synchronization
    ON item_races (starts_at, expires_at)
    WHERE status IN ('REGISTRATION', 'ACTIVE', 'REFUNDING');

CREATE TABLE item_race_participants
(
    id UUID PRIMARY KEY,
    race_id UUID NOT NULL REFERENCES item_races (id) ON DELETE CASCADE,
    account_id UUID NOT NULL,
    account_nickname VARCHAR(20) NOT NULL,
    confirmed_amount BIGINT NOT NULL DEFAULT 0,
    pending_amount BIGINT NOT NULL DEFAULT 0,
    status VARCHAR(24) NOT NULL,

    CONSTRAINT uq_item_race_participants_account
        UNIQUE (race_id, account_id),
    CONSTRAINT ck_item_race_participants_status
        CHECK (status IN (
            'PENDING',
            'ACTIVE',
            'PAYOUT_PENDING',
            'PAYOUT_FAILED',
            'WON',
            'LOST',
            'REFUND_PENDING',
            'REFUND_FAILED',
            'REFUNDED'
        )),
    CONSTRAINT ck_item_race_participants_amounts
        CHECK (confirmed_amount >= 0 AND pending_amount >= 0),
    CONSTRAINT ck_item_race_participants_target
        CHECK (confirmed_amount + pending_amount <= 5000)
);

CREATE INDEX idx_item_race_participants_account
    ON item_race_participants (account_id, id DESC);

CREATE TABLE wallet_operations
(
    id UUID PRIMARY KEY,
    request_id UUID NOT NULL UNIQUE,
    account_id UUID NOT NULL,
    initiated_by_user_id UUID,
    request_type VARCHAR(32),
    idempotency_key UUID,
    amount BIGINT NOT NULL,
    kind VARCHAR(16) NOT NULL,
    event_bet_id UUID REFERENCES event_bets (id),
    event_bet_resolution_id UUID REFERENCES event_bet_round_resolutions (id),
    race_participant_id UUID REFERENCES item_race_participants (id),
    created_at TIMESTAMPTZ NOT NULL,
    eligible_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(16) NOT NULL,
    wallet_transaction_id UUID,

    CONSTRAINT ck_wallet_operations_amount
        CHECK (amount > 0),
    CONSTRAINT ck_wallet_operations_kind
        CHECK (kind IN ('STAKE', 'PAYOUT', 'REFUND')),
    CONSTRAINT ck_wallet_operations_status
        CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED')),
    CONSTRAINT ck_wallet_operations_aggregate
        CHECK (
            (event_bet_id IS NOT NULL AND race_participant_id IS NULL)
            OR
            (event_bet_id IS NULL AND race_participant_id IS NOT NULL)
        )
);

CREATE UNIQUE INDEX uq_wallet_operations_idempotency_key
    ON wallet_operations (idempotency_key)
    WHERE idempotency_key IS NOT NULL;

CREATE INDEX idx_wallet_operations_request_type
    ON wallet_operations (request_type)
    WHERE request_type IS NOT NULL;

CREATE INDEX idx_wallet_operations_initiator
    ON wallet_operations (initiated_by_user_id, created_at DESC)
    WHERE initiated_by_user_id IS NOT NULL;

CREATE UNIQUE INDEX uq_wallet_event_bet_credit
    ON wallet_operations (event_bet_id, kind)
    WHERE event_bet_id IS NOT NULL
      AND kind IN ('PAYOUT', 'REFUND')
      AND status IN ('PENDING', 'COMPLETED');

CREATE UNIQUE INDEX uq_wallet_race_participant_credit
    ON wallet_operations (race_participant_id, kind)
    WHERE race_participant_id IS NOT NULL
      AND kind IN ('PAYOUT', 'REFUND')
      AND status IN ('PENDING', 'COMPLETED');

CREATE INDEX idx_wallet_operations_pending
    ON wallet_operations (status, eligible_at)
    WHERE status = 'PENDING';

CREATE TABLE global_occurrences
(
    event_id UUID PRIMARY KEY,
    event_type VARCHAR(16) NOT NULL,
    occurred_at TIMESTAMPTZ NOT NULL,

    CONSTRAINT ck_global_occurrences_type
        CHECK (event_type IN ('MOON', 'ECLIPSE', 'EXPLOSION'))
);

CREATE INDEX idx_global_occurrences_type_time
    ON global_occurrences (event_type, occurred_at);
