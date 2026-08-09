package com.mnemos.wallet.contracts.wallet.command

import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import java.util.UUID

data class WalletCreateTransactionCommand(
    val requestId: UUID,
    val accountId: UUID,

    val currency: Currency,
    val type: TransactionType,

    val source: TransactionSource,
    val sourceReferenceId: UUID,

    val amount: Long,
)
