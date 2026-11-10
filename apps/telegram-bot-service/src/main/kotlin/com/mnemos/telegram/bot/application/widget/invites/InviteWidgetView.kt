package com.mnemos.telegram.bot.application.widget.invites

import java.util.UUID

data class InviteWidgetView(
    val accountId: UUID,
    val accountName: String,
    val invites: List<InviteWidgetItem>,
    val totalInvites: Int,
    val page: Int,
    val totalPages: Int,
    val pendingRevokeId: UUID? = null,
)
