CREATE TABLE star_prime_purchases (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL,
    telegram_id BIGINT NOT NULL,
    package_id VARCHAR(64) NOT NULL,
    prime_amount BIGINT NOT NULL,
    stars_amount INTEGER NOT NULL,
    invoice_payload VARCHAR(128) NOT NULL UNIQUE,
    status VARCHAR(32) NOT NULL,
    telegram_payment_charge_id VARCHAR(255) UNIQUE,
    created_at TIMESTAMPTZ NOT NULL,
    expires_at TIMESTAMPTZ NOT NULL,
    pre_checkout_approved_at TIMESTAMPTZ,
    paid_at TIMESTAMPTZ,
    fulfilled_at TIMESTAMPTZ,
    updated_at TIMESTAMPTZ NOT NULL,
    CONSTRAINT star_prime_purchase_prime_amount_check CHECK (prime_amount > 0),
    CONSTRAINT star_prime_purchase_stars_amount_check CHECK (stars_amount > 0),
    CONSTRAINT star_prime_purchase_status_check CHECK (
        status IN (
            'CREATED',
            'INVOICE_READY',
            'PRE_CHECKOUT_APPROVED',
            'PAID',
            'FULFILLED',
            'FULFILLMENT_FAILED'
        )
    )
);

CREATE INDEX idx_star_prime_purchases_user_created
    ON star_prime_purchases (user_id, created_at DESC);

CREATE INDEX idx_star_prime_purchases_status_updated
    ON star_prime_purchases (status, updated_at);
