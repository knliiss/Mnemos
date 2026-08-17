package com.mnemos.moderation.application.wallet.usecase.create

import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.TransactionType
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import java.util.UUID

data class CreateWalletAdjustmentCommand(
    val targetType: WalletOwnerType,
    val targetId: UUID,
    val currency: Currency,
    val type: TransactionType,
    val amount: Long,
    val reason: String,
    val idempotencyKey: UUID? = null,
)
