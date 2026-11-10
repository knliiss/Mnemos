package com.mnemos.telegram.bot.application.profilecomments

import java.time.Instant
import java.util.UUID

data class ProfileCommentEntry(
    val id: UUID,
    val authorAccountId: UUID,
    val content: String?,
    val deleted: Boolean,
    val likes: Long,
    val replies: Long,
    val createdAt: Instant,
)
