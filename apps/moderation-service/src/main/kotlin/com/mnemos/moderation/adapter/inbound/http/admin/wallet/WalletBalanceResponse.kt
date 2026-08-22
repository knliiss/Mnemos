package com.mnemos.moderation.adapter.inbound.http.admin.wallet

import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import java.util.UUID

data class WalletBalanceResponse(
    val targetType: WalletOwnerType,
    val targetId: UUID,
    val currency: Currency,
    val amount: Long,
)
