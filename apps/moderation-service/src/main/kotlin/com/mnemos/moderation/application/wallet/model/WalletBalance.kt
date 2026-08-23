package com.mnemos.moderation.application.wallet.model

import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import java.util.UUID

data class WalletBalance(
    val ownerType: WalletOwnerType,
    val ownerId: UUID,
    val currency: Currency,
    val amount: Long,
)
