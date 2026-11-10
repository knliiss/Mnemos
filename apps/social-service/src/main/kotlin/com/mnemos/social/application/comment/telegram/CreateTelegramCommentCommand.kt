package com.mnemos.social.application.comment.telegram

import com.mnemos.social.application.actor.model.SocialActor
import java.util.UUID

data class CreateTelegramCommentCommand(
    val telegramId: Long,
    val actor: SocialActor,
    val operationId: UUID,
    val targetAccountId: UUID,
    val parentCommentId: UUID?,
    val content: String,
)
