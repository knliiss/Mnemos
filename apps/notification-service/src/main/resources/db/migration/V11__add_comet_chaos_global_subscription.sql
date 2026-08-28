ALTER TABLE global_subscriptions
    DROP CONSTRAINT ck_global_subscriptions_event_type;

ALTER TABLE global_subscriptions
    ADD CONSTRAINT ck_global_subscriptions_event_type
        CHECK (event_type IN (
            'DARKNESS',
            'MOON',
            'ECLIPSE',
            'EXPLOSION',
            'COMET_CHAOS'
        ));
