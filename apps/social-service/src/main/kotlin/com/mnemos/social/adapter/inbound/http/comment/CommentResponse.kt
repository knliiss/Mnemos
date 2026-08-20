package com.mnemos.social.adapter.inbound.http.comment

import java.time.Instant
import java.util.UUID

data class CommentResponse(
    val id: UUID,
    val targetAccountId: UUID,
    val authorAccountId: UUID,
    val parentCommentId: UUID?,
    val replyToAuthorAccountId: UUID?,
    val content: String?,
    val deleted: Boolean,
    val canDelete: Boolean,
    val likes: Long,
    val replies: Long,
    val createdAt: Instant,
    val updatedAt: Instant,
)
