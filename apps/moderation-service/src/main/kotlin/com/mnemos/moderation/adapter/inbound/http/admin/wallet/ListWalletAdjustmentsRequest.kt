package com.mnemos.moderation.adapter.inbound.http.admin.wallet

import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import java.util.UUID

data class ListWalletAdjustmentsRequest(
    val targetType: WalletOwnerType,
    val targetId: UUID,
)
