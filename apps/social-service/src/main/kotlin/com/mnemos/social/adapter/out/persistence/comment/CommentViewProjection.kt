package com.mnemos.social.adapter.out.persistence.comment

import java.time.Instant
import java.util.UUID

interface CommentViewProjection {
    val id: UUID
    val targetAccountId: UUID
    val authorUserId: UUID
    val authorAccountId: UUID
    val parentCommentId: UUID?
    val replyToAuthorAccountId: UUID?
    val content: String?
    val deleted: Boolean
    val likes: Long
    val replies: Long
    val createdAt: Instant
    val updatedAt: Instant
}
