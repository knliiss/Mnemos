ALTER TABLE star_prime_purchases
    ADD COLUMN telegram_request_id VARCHAR(255),
    ADD COLUMN invoice_url TEXT;

CREATE UNIQUE INDEX uq_star_prime_purchases_telegram_request_id
    ON star_prime_purchases (telegram_request_id)
    WHERE telegram_request_id IS NOT NULL;
