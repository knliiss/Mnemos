package com.mnemos.personalization.application.revenue.usecase.failwallet

import com.mnemos.personalization.application.wallet.model.WalletResultContext

interface FailCreatorRevenueWalletUseCase {
    fun execute(
        result: WalletResultContext,
        reason: String,
    )
}
