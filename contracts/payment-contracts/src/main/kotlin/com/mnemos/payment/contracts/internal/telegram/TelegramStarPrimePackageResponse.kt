package com.mnemos.payment.contracts.internal.telegram

data class TelegramStarPrimePackageResponse(
    val id: String,
    val primeAmount: Long,
    val starsAmount: Int,
)
