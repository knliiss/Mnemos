package com.mnemos.moderation.application.wallet.usecase.query

import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import java.util.UUID

data class ListWalletAdjustmentsQuery(
    val targetType: WalletOwnerType,
    val targetId: UUID,
)
