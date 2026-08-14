package com.mnemos.wallet.contracts.wallet.shared

import java.util.UUID

data class WalletOwner(
    val type: WalletOwnerType,
    val id: UUID,
)
