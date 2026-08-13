package com.mnemos.bet.domain.wallet.model

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

@JvmInline
value class WalletRequestId(
    val id: UUID,
) {
    companion object {
        fun generate(): WalletRequestId = WalletRequestId(UuidCreator.getTimeOrderedEpoch())
    }
}
