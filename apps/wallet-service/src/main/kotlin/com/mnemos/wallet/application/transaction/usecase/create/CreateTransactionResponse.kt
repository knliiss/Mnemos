package com.mnemos.wallet.application.transaction.usecase.create

import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.domain.transaction.model.RequestId
import com.mnemos.wallet.domain.transaction.model.TransactionId
import com.mnemos.wallet.domain.transaction.model.TransactionSourceReferenceId
import com.mnemos.wallet.domain.wallet.model.WalletOwner

data class CreateTransactionResponse(
    val transactionId: TransactionId,
    val requestId: RequestId,
    val owner: WalletOwner,
    val currency: Currency,
    val type: TransactionType,
    val source: TransactionSource,
    val sourceReferenceId: TransactionSourceReferenceId,
    val amount: Long,
)
