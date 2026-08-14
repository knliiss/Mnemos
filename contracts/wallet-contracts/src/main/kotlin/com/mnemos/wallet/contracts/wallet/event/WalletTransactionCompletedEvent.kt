package com.mnemos.wallet.contracts.wallet.event

import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletOwner
import java.time.Instant
import java.util.UUID

data class WalletTransactionCompletedEvent(
    val requestId: UUID,
    val transactionId: UUID,
    val owner: WalletOwner,
    val currency: Currency,
    val type: TransactionType,
    val source: TransactionSource,
    val sourceReferenceId: UUID,
    val amount: Long,
    val occurredAt: Instant,
)
