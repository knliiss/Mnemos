package com.mnemos.moderation.application.wallet.port.out

import com.mnemos.moderation.application.wallet.model.WalletBalance
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
import java.util.UUID

fun interface ModerationWalletBalanceReader {
    fun getBalance(
        ownerType: WalletOwnerType,
        ownerId: UUID,
    ): WalletBalance
}
