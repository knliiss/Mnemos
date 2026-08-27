package com.mnemos.wallet.application.exchange

data class CoinPurchaseCatalog(
    val coinPerPrime: Long,
    val options: List<CoinPurchaseOption>,
)
