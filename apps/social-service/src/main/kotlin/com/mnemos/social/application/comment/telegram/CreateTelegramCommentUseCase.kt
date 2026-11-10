package com.mnemos.social.application.comment.telegram

import java.util.UUID

fun interface CreateTelegramCommentUseCase {
    fun execute(command: CreateTelegramCommentCommand): TelegramCommentCreationResult
}

sealed interface TelegramCommentCreationResult {
    data class Created(val commentId: UUID) : TelegramCommentCreationResult
    data object Conflict : TelegramCommentCreationResult
}
