package com.mnemos.payment.contracts.internal.telegram

data class ValidateStarPreCheckoutRequest(
    val telegramId: Long,
    val invoicePayload: String,
    val currency: String,
    val totalAmount: Int,
)
