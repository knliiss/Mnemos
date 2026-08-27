package com.mnemos.wallet.adapter.inbound.http.exchange

import jakarta.validation.constraints.Positive
import java.util.UUID

data class CoinPurchaseRequest(
    val requestId: UUID,
    @field:Positive
    val primeAmount: Long,
)
