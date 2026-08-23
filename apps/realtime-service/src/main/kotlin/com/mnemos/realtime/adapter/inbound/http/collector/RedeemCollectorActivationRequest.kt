package com.mnemos.realtime.adapter.inbound.http.collector

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class RedeemCollectorActivationRequest(
    @field:Pattern(regexp = "^[A-Za-z0-9_-]{43}$")
    val token: String,
    @field:NotBlank
    @field:Size(max = 48)
    @field:Pattern(regexp = "^[\\p{L}\\p{N} ._-]+$")
    val deviceName: String,
    @field:Pattern(regexp = "^[0-9a-f]{64}$")
    val secretHash: String,
)
