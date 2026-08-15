CREATE TABLE comments
(
    id                UUID PRIMARY KEY,
    target_account_id UUID         NOT NULL,
    author_user_id    UUID         NOT NULL,
    author_account_id UUID         NOT NULL,
    parent_comment_id UUID,
    content           VARCHAR(500) NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL,
    updated_at        TIMESTAMPTZ  NOT NULL,
    deleted_at        TIMESTAMPTZ,

    CONSTRAINT fk_comments_parent
        FOREIGN KEY (parent_comment_id) REFERENCES comments (id),
    CONSTRAINT ck_comments_not_empty
        CHECK (char_length(trim(content)) > 0)
);

CREATE INDEX idx_comments_target_root_created
    ON comments (target_account_id, created_at DESC)
    WHERE parent_comment_id IS NULL;

CREATE INDEX idx_comments_parent_created
    ON comments (parent_comment_id, created_at ASC)
    WHERE parent_comment_id IS NOT NULL;

CREATE TABLE account_reactions
(
    actor_account_id  UUID        NOT NULL,
    target_account_id UUID        NOT NULL,
    type              VARCHAR(8)  NOT NULL,
    created_at        TIMESTAMPTZ NOT NULL,
    updated_at        TIMESTAMPTZ NOT NULL,

    PRIMARY KEY (actor_account_id, target_account_id),
    CONSTRAINT ck_account_reactions_type CHECK (type IN ('LIKE', 'DISLIKE')),
    CONSTRAINT ck_account_reactions_not_self CHECK (actor_account_id <> target_account_id)
);

CREATE INDEX idx_account_reactions_target
    ON account_reactions (target_account_id, type);

CREATE TABLE comment_likes
(
    comment_id       UUID        NOT NULL REFERENCES comments (id),
    actor_account_id UUID        NOT NULL,
    created_at       TIMESTAMPTZ NOT NULL,

    PRIMARY KEY (comment_id, actor_account_id)
);

CREATE INDEX idx_comment_likes_comment
    ON comment_likes (comment_id);
