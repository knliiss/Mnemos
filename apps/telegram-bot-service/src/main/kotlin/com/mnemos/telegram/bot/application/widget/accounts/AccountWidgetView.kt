package com.mnemos.telegram.bot.application.widget.accounts

import java.util.UUID

data class AccountWidgetView(
    val activeAccountId: UUID?,
    val accounts: List<AccountWidgetEntry>,
    val totalAccounts: Int,
    val page: Int,
    val totalPages: Int,
    val coin: Long?,
    val prime: Long?,
    val feedback: String? = null,
)
