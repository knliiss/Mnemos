DROP INDEX IF EXISTS idx_personalizations_marketplace;

CREATE INDEX idx_personalizations_marketplace
    ON personalizations (
        type,
        price_currency,
        created_at DESC,
        id DESC
    )
    WHERE status = 'PUBLISHED' AND visibility = 'PUBLIC';

CREATE INDEX idx_personalizations_marketplace_created_at
    ON personalizations (
        created_at DESC,
        id DESC
    )
    WHERE status = 'PUBLISHED' AND visibility = 'PUBLIC';
