package com.mnemos.telegram.bot.application.profilecomments.port.out

import com.mnemos.telegram.bot.application.profilecomments.ProfileCommentCreationResult
import java.util.UUID

fun interface ProfileCommentMutationGateway {
    fun create(
        telegramId: Long,
        targetAccountId: UUID,
        expectedActorAccountId: UUID,
        operationId: UUID,
        parentCommentId: UUID?,
        content: String,
    ): ProfileCommentCreationResult
}
