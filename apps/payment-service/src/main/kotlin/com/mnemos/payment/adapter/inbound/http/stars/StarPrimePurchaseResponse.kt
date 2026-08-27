package com.mnemos.payment.adapter.inbound.http.stars

import com.mnemos.payment.domain.stars.model.StarPrimePurchaseStatus
import java.util.UUID

data class StarPrimePurchaseResponse(
    val purchaseId: UUID,
    val packageId: String,
    val primeAmount: Long,
    val starsAmount: Int,
    val status: StarPrimePurchaseStatus,
)
