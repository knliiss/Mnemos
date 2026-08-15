CREATE TABLE notification_pack_selection_revisions
(
    user_id  UUID   PRIMARY KEY,
    revision BIGINT NOT NULL,

    CONSTRAINT ck_notification_pack_selection_revisions_revision
        CHECK (revision >= 0)
);
