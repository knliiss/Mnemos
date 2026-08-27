package com.mnemos.payment.adapter.inbound.http.stars

data class StarPrimePackageResponse(
    val id: String,
    val primeAmount: Long,
    val starsAmount: Int,
)
