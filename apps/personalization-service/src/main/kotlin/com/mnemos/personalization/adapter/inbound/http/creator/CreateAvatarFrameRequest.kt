package com.mnemos.personalization.adapter.inbound.http.creator

import com.mnemos.personalization.adapter.inbound.http.personalization.GlowStyleRequest
import com.mnemos.personalization.adapter.inbound.http.personalization.PersonalizationPriceRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CreateAvatarFrameRequest(
    @field:NotBlank
    @field:Size(min = 3, max = 64)
    val name: String,
    @field:Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    val borderColor: String,
    @field:Min(1)
    @field:Max(8)
    val borderWidthPx: Int,
    @field:Min(0)
    @field:Max(32)
    val radiusPx: Int,
    @field:Valid
    val glow: GlowStyleRequest?,
    @field:Valid
    val price: PersonalizationPriceRequest,
)
