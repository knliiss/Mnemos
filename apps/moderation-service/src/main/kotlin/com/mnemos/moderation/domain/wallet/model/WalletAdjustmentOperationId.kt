package com.mnemos.moderation.domain.wallet.model

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

@JvmInline
value class WalletAdjustmentOperationId(
    val id: UUID,
) {
    companion object {
        fun generate(): WalletAdjustmentOperationId = WalletAdjustmentOperationId(UuidCreator.getTimeOrderedEpoch())
    }
}
