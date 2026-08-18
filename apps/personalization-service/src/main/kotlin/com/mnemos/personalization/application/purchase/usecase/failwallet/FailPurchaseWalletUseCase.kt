package com.mnemos.personalization.application.purchase.usecase.failwallet

import com.mnemos.personalization.application.wallet.model.WalletResultContext

interface FailPurchaseWalletUseCase {
    fun execute(
        result: WalletResultContext,
        reason: String,
    )
}
