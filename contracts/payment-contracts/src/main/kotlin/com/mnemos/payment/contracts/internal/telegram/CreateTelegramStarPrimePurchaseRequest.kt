package com.mnemos.payment.contracts.internal.telegram

data class CreateTelegramStarPrimePurchaseRequest(
    val telegramId: Long,
    val packageId: String,
    val requestId: String,
)
