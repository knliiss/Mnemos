package com.mnemos.bet.application.wallet.usecase.process

import com.mnemos.bet.domain.wallet.model.WalletRequestId
import com.mnemos.wallet.contracts.wallet.shared.WalletTransactionFailureReason

data class ProcessWalletTransactionFailedCommand(
    val requestId: WalletRequestId,
    val reason: WalletTransactionFailureReason,
)
