package com.mnemos.social.contracts.internal.v1

import java.time.Instant
import java.util.UUID

data class InternalTelegramCommentPage(
    val items: List<InternalTelegramComment>,
    val page: Int,
    val totalPages: Int,
    val totalItems: Long,
)

data class InternalTelegramComment(
    val id: UUID,
    val authorAccountId: UUID,
    val content: String?,
    val deleted: Boolean,
    val likes: Long,
    val replies: Long,
    val createdAt: Instant,
)
