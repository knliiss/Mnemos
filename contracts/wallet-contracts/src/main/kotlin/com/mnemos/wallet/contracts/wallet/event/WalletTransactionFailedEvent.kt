package com.mnemos.wallet.contracts.wallet.event

import com.mnemos.wallet.contracts.wallet.shared.WalletTransactionFailureReason
import java.util.UUID

data class WalletTransactionFailedEvent(
    val requestId: UUID,
    val reason: WalletTransactionFailureReason
)
