package com.mnemos.telegram.bot.application.widget.invites.port.out

import java.util.UUID

interface InviteWidgetMutationGuard {
    fun claimQuery(telegramId: Long, queryId: String): Boolean

    fun requestRevocation(telegramId: Long, messageId: Int, inviteId: UUID)

    fun consumeRevocation(telegramId: Long, messageId: Int, inviteId: UUID): Boolean

    fun cancelRevocation(telegramId: Long, messageId: Int)
}
