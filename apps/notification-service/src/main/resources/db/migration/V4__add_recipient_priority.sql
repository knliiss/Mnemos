ALTER TABLE recipients
    ADD COLUMN priority VARCHAR(16) NOT NULL DEFAULT 'STANDARD';

ALTER TABLE recipients
    ADD CONSTRAINT ck_recipients_priority
        CHECK (priority IN (
                            'STANDARD',
                            'ELEVATED',
                            'PRIORITY'
            ));