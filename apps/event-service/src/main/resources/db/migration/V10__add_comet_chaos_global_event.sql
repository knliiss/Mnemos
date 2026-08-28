ALTER TABLE global_events
    DROP CONSTRAINT ck_global_events_type;

ALTER TABLE global_events
    ADD CONSTRAINT ck_global_events_type
        CHECK (type IN (
            'DARKNESS',
            'MOON',
            'ECLIPSE',
            'EXPLOSION',
            'COMET_CHAOS'
        ));
