package com.mnemos.wallet.adapter.inbound.http.transaction

import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import java.time.Instant
import java.util.UUID

data class TransactionResponse(
    val id: UUID,
    val requestId: UUID,
    val currency: Currency,
    val type: TransactionType,
    val source: TransactionSource,
    val sourceReferenceId: UUID,
    val amount: Long,
    val createdAt: Instant,
)
