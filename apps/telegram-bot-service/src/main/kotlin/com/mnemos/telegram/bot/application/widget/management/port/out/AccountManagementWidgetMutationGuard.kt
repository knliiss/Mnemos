package com.mnemos.telegram.bot.application.widget.management.port.out

import java.util.UUID

interface AccountManagementWidgetMutationGuard {
    fun claimQuery(telegramId: Long, queryId: String): Boolean
    fun requestKick(telegramId: Long, messageId: Int, accountId: UUID, memberId: UUID)
    fun consumeKick(telegramId: Long, messageId: Int, accountId: UUID, memberId: UUID): Boolean
    fun cancelKick(telegramId: Long, messageId: Int)
}
