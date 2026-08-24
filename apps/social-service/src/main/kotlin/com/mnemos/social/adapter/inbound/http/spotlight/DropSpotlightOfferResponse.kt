package com.mnemos.social.adapter.inbound.http.spotlight

data class DropSpotlightOfferResponse(
    val pricePrime: Long,
    val purchaseWindowSeconds: Long,
    val buyerCooldownSeconds: Long,
)
