package com.mnemos.bet.application.wallet.usecase.process

import com.mnemos.bet.domain.wallet.model.WalletRequestId

data class ProcessWalletTransactionFailedCommand(
    val requestId: WalletRequestId,
)
