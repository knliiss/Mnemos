package com.mnemos.telegram.bot.application.widget.invites

import com.mnemos.telegram.bot.application.account.invite.model.TelegramAccountInviteStatus
import java.time.Instant
import java.util.UUID

data class InviteWidgetItem(
    val id: UUID,
    val code: String,
    val status: TelegramAccountInviteStatus,
    val expiresAt: Instant?,
)
