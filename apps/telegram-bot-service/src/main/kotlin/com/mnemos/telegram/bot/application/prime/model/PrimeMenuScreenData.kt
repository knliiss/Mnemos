package com.mnemos.telegram.bot.application.prime.model

data class PrimeMenuScreenData(
    val packages: List<PrimePurchasePackage>?,
    val invoice: PrimePurchaseInvoice?,
    val notice: PrimePurchaseNotice?,
)
