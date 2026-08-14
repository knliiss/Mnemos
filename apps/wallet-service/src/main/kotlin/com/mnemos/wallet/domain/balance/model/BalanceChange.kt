package com.mnemos.wallet.domain.balance.model

import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.domain.transaction.model.TransactionAmount
import com.mnemos.wallet.domain.transaction.model.TransactionId
import com.mnemos.wallet.domain.wallet.model.WalletOwner
import java.time.Instant

data class BalanceChange(
    val transactionId: TransactionId,
    val owner: WalletOwner,
    val currency: Currency,
    val type: TransactionType,
    val amount: TransactionAmount,
    val appliedAt: Instant,
)
