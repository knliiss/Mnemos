package com.mnemos.telegram.bot.application.prime.model

data class PrimePurchaseInvoice(
    val primeAmount: Long,
    val starsAmount: Int,
    val url: String,
)
