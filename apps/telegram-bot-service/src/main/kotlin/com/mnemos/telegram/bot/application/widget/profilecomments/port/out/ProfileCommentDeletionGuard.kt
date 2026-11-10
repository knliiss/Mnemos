package com.mnemos.telegram.bot.application.widget.profilecomments.port.out

import java.util.UUID

interface ProfileCommentDeletionGuard {
    fun request(telegramId: Long, messageId: Int, profileId: UUID, commentId: UUID, actorAccountId: UUID)
    fun consume(telegramId: Long, messageId: Int, profileId: UUID, commentId: UUID, actorAccountId: UUID): Boolean
    fun cancel(telegramId: Long, messageId: Int)
}
