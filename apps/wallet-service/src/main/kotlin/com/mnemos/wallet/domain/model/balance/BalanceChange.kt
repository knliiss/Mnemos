package com.mnemos.wallet.domain.model.balance

import com.mnemos.common.core.model.id.AccountId
import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.domain.model.transaction.TransactionAmount
import com.mnemos.wallet.domain.model.transaction.TransactionId
import java.time.Instant

data class BalanceChange(
    val transactionId: TransactionId,
    val accountId: AccountId,
    val currency: Currency,
    val type: TransactionType,
    val amount: TransactionAmount,
    val appliedAt: Instant,
)
