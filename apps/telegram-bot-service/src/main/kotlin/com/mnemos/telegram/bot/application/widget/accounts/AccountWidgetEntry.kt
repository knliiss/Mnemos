package com.mnemos.telegram.bot.application.widget.accounts

import java.util.UUID

data class AccountWidgetEntry(
    val accountId: UUID,
    val nickname: String,
    val owner: Boolean,
    val memberCount: Int,
    val maxMembers: Int,
    val active: Boolean,
    val enabled: Boolean,
)
