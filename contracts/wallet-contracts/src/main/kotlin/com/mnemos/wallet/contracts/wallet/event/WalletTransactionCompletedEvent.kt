package com.mnemos.wallet.contracts.wallet.event

import java.util.UUID

data class WalletTransactionCompletedEvent(
    val requestId: UUID,
    val transactionId: UUID,
    val accountId: UUID,
    val sourceReferenceId: UUID
)
