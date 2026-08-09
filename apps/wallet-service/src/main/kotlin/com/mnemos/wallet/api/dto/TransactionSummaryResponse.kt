package com.mnemos.wallet.api.dto

import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.infrastructure.persistence.transaction.TransactionEntity
import java.time.Instant
import java.util.UUID

data class TransactionSummaryResponse(
    val id: UUID,
    val currency: Currency,
    val type: TransactionType,
    val source: TransactionSource,
    val amount: Long,
    val createdAt: Instant,
)

fun TransactionEntity.toSummaryResponse(): TransactionSummaryResponse =
    TransactionSummaryResponse(
        id = id,
        currency = currency,
        type = type,
        source = source,
        amount = amount,
        createdAt = createdAt,
    )
