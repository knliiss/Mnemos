package com.mnemos.telegram.bot.application.profilecomments

sealed interface ProfileCommentsPageResult {
    data class Available(val page: ProfileCommentsPage) : ProfileCommentsPageResult
    data object Unavailable : ProfileCommentsPageResult
}

data class ProfileCommentsPage(
    val entries: List<ProfileCommentEntry>,
    val page: Int,
    val totalPages: Int,
    val totalItems: Long,
)
