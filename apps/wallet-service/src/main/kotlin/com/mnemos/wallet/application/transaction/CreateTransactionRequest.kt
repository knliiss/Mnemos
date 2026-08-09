package com.mnemos.wallet.application.transaction

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.domain.model.transaction.RequestId
import com.mnemos.wallet.domain.model.transaction.TransactionSourceReferenceId

data class CreateTransactionRequest(
    val requestId: RequestId,
    val accountId: AccountId,
    val currency: Currency,
    val type: TransactionType,
    val source: TransactionSource,
    val sourceReferenceId: TransactionSourceReferenceId,
    val amount: Long,
)
