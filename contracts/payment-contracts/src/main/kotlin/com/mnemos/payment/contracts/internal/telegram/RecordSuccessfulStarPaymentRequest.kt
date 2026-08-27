package com.mnemos.payment.contracts.internal.telegram

data class RecordSuccessfulStarPaymentRequest(
    val telegramId: Long,
    val invoicePayload: String,
    val currency: String,
    val totalAmount: Int,
    val telegramPaymentChargeId: String,
)
