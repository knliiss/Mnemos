package com.mnemos.wallet.application.transaction.usecase.create

import com.mnemos.wallet.contracts.wallet.shared.WalletTransactionFailureReason
import com.mnemos.wallet.domain.transaction.model.RequestId

sealed interface CreateTransactionResult {
    data class Completed(
        val response: CreateTransactionResponse,
    ) : CreateTransactionResult

    data class Failed(
        val requestId: RequestId,
        val reason: WalletTransactionFailureReason,
    ) : CreateTransactionResult
}
