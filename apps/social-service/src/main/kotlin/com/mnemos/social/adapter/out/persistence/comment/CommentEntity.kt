package com.mnemos.social.adapter.out.persistence.comment

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant
import java.util.UUID

@Entity
@Table(name = "comments")
class CommentEntity(
    @Id
    @Column(name = "id", nullable = false)
    var id: UUID,
    @Column(name = "target_account_id", nullable = false)
    var targetAccountId: UUID,
    @Column(name = "author_user_id", nullable = false)
    var authorUserId: UUID,
    @Column(name = "author_account_id", nullable = false)
    var authorAccountId: UUID,
    @Column(name = "parent_comment_id")
    var parentCommentId: UUID?,
    @Column(name = "content", nullable = false, length = 500)
    var content: String,
    @Column(name = "created_at", nullable = false)
    var createdAt: Instant,
    @Column(name = "updated_at", nullable = false)
    var updatedAt: Instant,
    @Column(name = "deleted_at")
    var deletedAt: Instant?,
)
