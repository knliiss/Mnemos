package com.mnemos.bet.application.wallet.usecase.process

import com.mnemos.bet.domain.wallet.model.WalletRequestId
import com.mnemos.bet.domain.wallet.model.WalletTransactionId

data class ProcessWalletTransactionCompletedCommand(
    val requestId: WalletRequestId,
    val transactionId: WalletTransactionId,
)
