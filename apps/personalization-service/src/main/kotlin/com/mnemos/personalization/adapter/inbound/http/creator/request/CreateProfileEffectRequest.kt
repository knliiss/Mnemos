package com.mnemos.personalization.adapter.inbound.http.creator.request

import com.mnemos.personalization.adapter.inbound.http.personalization.request.PersonalizationPriceRequest
import com.mnemos.personalization.domain.personalization.model.EffectType
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CreateProfileEffectRequest(
    @field:NotBlank
    @field:Size(min = 3, max = 64)
    val name: String,
    val effect: EffectType,
    @field:Min(1)
    @field:Max(100)
    val intensity: Int,
    @field:Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    val primaryColor: String,
    @field:Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    val secondaryColor: String?,
    @field:Valid
    val price: PersonalizationPriceRequest,
)
