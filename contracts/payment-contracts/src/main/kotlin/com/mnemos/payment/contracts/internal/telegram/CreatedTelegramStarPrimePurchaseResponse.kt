package com.mnemos.payment.contracts.internal.telegram

import java.util.UUID

data class CreatedTelegramStarPrimePurchaseResponse(
    val purchaseId: UUID,
    val packageId: String,
    val primeAmount: Long,
    val starsAmount: Int,
    val invoiceUrl: String,
)
