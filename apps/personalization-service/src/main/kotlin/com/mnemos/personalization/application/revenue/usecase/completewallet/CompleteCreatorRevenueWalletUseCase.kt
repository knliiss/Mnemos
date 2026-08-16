package com.mnemos.personalization.application.revenue.usecase.completewallet

import com.mnemos.personalization.application.wallet.model.WalletResultContext
import java.util.UUID

interface CompleteCreatorRevenueWalletUseCase {
    fun execute(
        result: WalletResultContext,
        transactionId: UUID,
    )
}
