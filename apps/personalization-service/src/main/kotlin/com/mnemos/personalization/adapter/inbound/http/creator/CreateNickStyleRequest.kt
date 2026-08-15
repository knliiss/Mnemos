package com.mnemos.personalization.adapter.inbound.http.creator

import com.mnemos.personalization.adapter.inbound.http.personalization.GlowStyleRequest
import com.mnemos.personalization.adapter.inbound.http.personalization.GradientStopRequest
import com.mnemos.personalization.adapter.inbound.http.personalization.PersonalizationPriceRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateNickStyleRequest(
    @field:NotBlank
    @field:Size(min = 3, max = 64)
    val name: String,
    @field:Valid
    @field:Size(min = 1, max = 4)
    val colors: List<GradientStopRequest>,
    @field:Min(0)
    @field:Max(360)
    val angleDegrees: Int,
    @field:Valid
    val glow: GlowStyleRequest?,
    @field:Valid
    val price: PersonalizationPriceRequest,
)
