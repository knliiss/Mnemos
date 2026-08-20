ALTER TABLE comments
    ADD COLUMN reply_to_comment_id UUID;

ALTER TABLE comments
    ADD CONSTRAINT fk_comments_reply_target
        FOREIGN KEY (reply_to_comment_id) REFERENCES comments (id);

CREATE INDEX idx_comments_reply_target
    ON comments (reply_to_comment_id)
    WHERE reply_to_comment_id IS NOT NULL;
