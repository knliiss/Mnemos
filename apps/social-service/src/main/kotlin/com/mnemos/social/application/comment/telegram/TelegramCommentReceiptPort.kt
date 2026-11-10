package com.mnemos.social.application.comment.telegram

import com.mnemos.social.application.actor.model.SocialActor
import java.util.UUID

interface TelegramCommentReceiptPort {
    fun claim(
        operationId: UUID,
        telegramId: Long,
        actor: SocialActor,
        targetAccountId: UUID,
        payloadHash: String,
    ): TelegramCommentClaim

    fun complete(operationId: UUID, commentId: UUID)
}

sealed interface TelegramCommentClaim {
    data object Claimed : TelegramCommentClaim
    data class Replayed(val commentId: UUID) : TelegramCommentClaim
    data object Conflict : TelegramCommentClaim
}
