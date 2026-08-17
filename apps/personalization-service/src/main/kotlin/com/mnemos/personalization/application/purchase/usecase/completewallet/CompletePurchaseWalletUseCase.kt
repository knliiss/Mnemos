package com.mnemos.personalization.application.purchase.usecase.completewallet

import com.mnemos.personalization.application.wallet.model.WalletResultContext
import java.util.UUID

interface CompletePurchaseWalletUseCase {
    fun execute(
        result: WalletResultContext,
        transactionId: UUID,
    )
}
