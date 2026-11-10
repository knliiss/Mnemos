package com.mnemos.telegram.bot.application.profilecomments.port.out

import com.mnemos.telegram.bot.application.profilecomments.ProfileCommentCreationResult
import java.util.UUID

interface ProfileCommentMutationGateway {
    fun create(
        telegramId: Long,
        targetAccountId: UUID,
        expectedActorAccountId: UUID,
        operationId: UUID,
        parentCommentId: UUID?,
        content: String,
    ): ProfileCommentCreationResult

    fun edit(
        telegramId: Long,
        targetAccountId: UUID,
        commentId: UUID,
        expectedActorAccountId: UUID,
        content: String,
    ): ProfileCommentCreationResult

    fun delete(
        telegramId: Long,
        targetAccountId: UUID,
        commentId: UUID,
        expectedActorAccountId: UUID,
    ): ProfileCommentCreationResult
}
