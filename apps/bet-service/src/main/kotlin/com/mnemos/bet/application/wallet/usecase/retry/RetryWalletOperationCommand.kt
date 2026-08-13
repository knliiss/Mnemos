package com.mnemos.bet.application.wallet.usecase.retry

import com.mnemos.bet.domain.wallet.model.WalletOperationId

data class RetryWalletOperationCommand(
    val operationId: WalletOperationId,
)
