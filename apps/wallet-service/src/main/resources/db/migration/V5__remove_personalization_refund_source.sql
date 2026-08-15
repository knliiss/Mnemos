ALTER TABLE transactions
    DROP CONSTRAINT ck_transactions_source;

ALTER TABLE transactions
    ADD CONSTRAINT ck_transactions_source
        CHECK (source IN (
            'DAILY_REWARD',
            'ITEM_DROP_REWARD',
            'BET_PLACE',
            'BET_WIN',
            'BET_REFUND',
            'PERSONALIZATION_PURCHASE',
            'CREATOR_REVENUE',
            'SPOTLIGHT_PURCHASE',
            'PRIME_PURCHASE',
            'ADMIN_ADJUSTMENT'
        ));

ALTER TABLE transaction_requests
    DROP CONSTRAINT ck_transaction_requests_source;

ALTER TABLE transaction_requests
    ADD CONSTRAINT ck_transaction_requests_source
        CHECK (source IN (
            'DAILY_REWARD',
            'ITEM_DROP_REWARD',
            'BET_PLACE',
            'BET_WIN',
            'BET_REFUND',
            'PERSONALIZATION_PURCHASE',
            'CREATOR_REVENUE',
            'SPOTLIGHT_PURCHASE',
            'PRIME_PURCHASE',
            'ADMIN_ADJUSTMENT'
        ));
