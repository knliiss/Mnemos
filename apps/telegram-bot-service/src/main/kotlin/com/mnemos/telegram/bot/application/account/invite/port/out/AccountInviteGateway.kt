package com.mnemos.telegram.bot.application.account.invite.port.out

import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInviteListResult
import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInviteMutationResult
import java.util.UUID

interface AccountInviteGateway {
    fun getInvites(telegramId: Long): TelegramAccountInviteListResult

    fun createInvite(
        telegramId: Long,
        expiresInMinutes: Long?,
    ): TelegramAccountInviteMutationResult

    fun revokeInvite(
        telegramId: Long,
        inviteId: UUID,
    ): TelegramAccountInviteMutationResult
}
