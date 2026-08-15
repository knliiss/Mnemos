package com.mnemos.personalization.application.wallet.model

import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionSource
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import java.util.UUID

data class WalletResultContext(
    val requestId: UUID,
    val actorUserId: UUID?,
    val ownerType: WalletOwnerType,
    val ownerId: UUID,
    val currency: Currency,
    val transactionType: TransactionType,
    val source: TransactionSource,
    val sourceReferenceId: UUID,
    val amount: Long,
)
