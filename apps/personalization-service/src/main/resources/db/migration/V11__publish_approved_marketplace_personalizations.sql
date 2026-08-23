UPDATE personalizations
SET status = 'PUBLISHED',
    updated_at = CURRENT_TIMESTAMP
WHERE status = 'APPROVED'
  AND visibility = 'PUBLIC'
  AND price_currency IS NOT NULL
  AND price_amount IS NOT NULL;
