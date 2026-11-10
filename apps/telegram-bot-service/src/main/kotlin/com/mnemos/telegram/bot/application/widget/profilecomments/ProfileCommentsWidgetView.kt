package com.mnemos.telegram.bot.application.widget.profilecomments

import java.util.UUID

data class ProfileCommentsWidgetView(
    val profileId: UUID,
    val nickname: String,
    val page: Int,
    val totalPages: Int,
    val totalItems: Long,
    val rootCommentId: UUID?,
    val comments: List<ProfileCommentWidgetItem>,
    val feedback: String? = null,
)
