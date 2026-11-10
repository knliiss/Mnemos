package com.mnemos.telegram.bot.application.profilecomments

import java.util.UUID

sealed interface ProfileCommentCreationResult {
    data class Created(val commentId: UUID) : ProfileCommentCreationResult
    data object AccountRequired : ProfileCommentCreationResult
    data object Conflict : ProfileCommentCreationResult
    data object Invalid : ProfileCommentCreationResult
    data object Unavailable : ProfileCommentCreationResult
}
