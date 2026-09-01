package com.mnemos.telegram.bot.adapter.out.account

import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountInviteResponse
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountInviteStatus
import com.mnemos.contracts.account.internal.v1.InternalTelegramAccountInvitesResponse
import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInvite
import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInviteStatus
import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInvites

object AccountInviteApiMapper {
    fun InternalTelegramAccountInvitesResponse.toModel(): TelegramAccountInvites =
        TelegramAccountInvites(
            accountId = accountId,
            cristalixNickname = cristalixNickname,
            invites = invites.map { invite -> invite.toModel() },
        )

    private fun InternalTelegramAccountInviteResponse.toModel(): TelegramAccountInvite =
        TelegramAccountInvite(
            id = id,
            code = code,
            createdAt = createdAt,
            expiresAt = expiresAt,
            consumedAt = consumedAt,
            revokedAt = revokedAt,
            status = status.toModel(),
        )

    private fun InternalTelegramAccountInviteStatus.toModel(): TelegramAccountInviteStatus = TelegramAccountInviteStatus.valueOf(name)
}
