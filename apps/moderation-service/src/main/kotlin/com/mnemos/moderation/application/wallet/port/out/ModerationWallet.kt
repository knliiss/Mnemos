package com.mnemos.moderation.application.wallet.port.out

import com.mnemos.moderation.domain.wallet.model.WalletAdjustmentOperation

fun interface ModerationWallet {
    fun requestAdjustment(operation: WalletAdjustmentOperation)
}
