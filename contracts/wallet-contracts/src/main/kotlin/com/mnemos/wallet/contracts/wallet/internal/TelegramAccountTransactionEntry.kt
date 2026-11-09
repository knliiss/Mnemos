package com.mnemos.wallet.contracts.wallet.internal

import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import java.time.Instant
import java.util.UUID

data class TelegramAccountTransactionEntry(
    val id: UUID,
    val currency: Currency,
    val type: TransactionType,
    val source: TransactionSource,
    val amount: Long,
    val createdAt: Instant,
)
