package com.mnemos.payment.application.stars.model

import com.mnemos.payment.domain.stars.model.StarPrimePurchaseStatus
import java.util.UUID

data class CreatedStarPrimePurchase(
    val purchaseId: UUID,
    val packageId: String,
    val primeAmount: Long,
    val starsAmount: Int,
    val status: StarPrimePurchaseStatus,
    val invoiceUrl: String,
)
