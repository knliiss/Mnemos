package com.mnemos.social.application.comment.model

import com.mnemos.social.domain.comment.model.CommentId
import java.time.Instant
import java.util.UUID

data class CommentView(
    val id: CommentId,
    val targetAccountId: UUID,
    val authorAccountId: UUID,
    val parentCommentId: CommentId?,
    val content: String?,
    val deleted: Boolean,
    val likes: Long,
    val replies: Long,
    val createdAt: Instant,
    val updatedAt: Instant,
    val authorUserId: UUID? = null,
    val replyToAuthorAccountId: UUID? = null,
)
