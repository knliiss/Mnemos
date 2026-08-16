package com.mnemos.social.application.spotlight.usecase.processwallet

import com.mnemos.social.application.spotlight.model.SpotlightWalletResult
import java.util.UUID

interface ProcessDropSpotlightWalletResultUseCase {
    fun completed(
        result: SpotlightWalletResult,
        transactionId: UUID,
    )

    fun failed(result: SpotlightWalletResult)
}
