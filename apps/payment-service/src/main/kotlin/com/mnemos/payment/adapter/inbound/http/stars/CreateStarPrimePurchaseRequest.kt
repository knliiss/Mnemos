package com.mnemos.payment.adapter.inbound.http.stars

import jakarta.validation.constraints.NotBlank

data class CreateStarPrimePurchaseRequest(
    @field:NotBlank
    val packageId: String,
)
