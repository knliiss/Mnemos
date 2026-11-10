package com.mnemos.telegram.bot.application.widget.profilecomments

import java.util.UUID

data class ProfileCommentWidgetItem(
    val id: UUID,
    val author: String,
    val content: String,
    val deleted: Boolean,
    val likes: Long,
    val replies: Long,
    val createdAt: String,
)
