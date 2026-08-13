ALTER TABLE recipients
    ADD COLUMN delivery_status VARCHAR(16) NOT NULL DEFAULT 'ACTIVE';

ALTER TABLE recipients
    ADD CONSTRAINT ck_recipients_delivery_status
        CHECK (delivery_status IN ('ACTIVE', 'BLOCKED'));
