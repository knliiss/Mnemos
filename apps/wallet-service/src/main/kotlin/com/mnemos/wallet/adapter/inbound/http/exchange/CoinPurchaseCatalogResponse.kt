package com.mnemos.wallet.adapter.inbound.http.exchange

data class CoinPurchaseCatalogResponse(
    val coinPerPrime: Long,
    val options: List<CoinPurchaseOptionResponse>,
)
