package com.mnemos.wallet.application.transaction

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.domain.model.transaction.RequestId
import com.mnemos.wallet.domain.model.transaction.TransactionId
import com.mnemos.wallet.domain.model.transaction.TransactionSourceReferenceId

data class CreateTransactionResponse(
    val transactionId: TransactionId,
    val requestId: RequestId,
    val accountId: AccountId,
    val sourceReferenceId: TransactionSourceReferenceId,
)
