package com.mnemos.wallet.contracts.wallet.internal

import java.util.UUID

data class TelegramAccountTransactionsResponse(
    val accountId: UUID,
    val items: List<TelegramAccountTransactionEntry>,
    val page: Int,
    val totalPages: Int,
    val totalItems: Long,
)
