package com.mnemos.personalization.adapter.inbound.http.creator

import com.mnemos.personalization.adapter.inbound.http.personalization.GlowStyleRequest
import com.mnemos.personalization.adapter.inbound.http.personalization.PersonalizationPriceRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class CreateProfileThemeRequest(
    @field:NotBlank
    @field:Size(min = 3, max = 64)
    val name: String,
    @field:Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    val background: String,
    @field:Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    val surface: String,
    @field:Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    val accent: String,
    @field:Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    val text: String,
    @field:Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    val border: String,
    @field:Valid
    val glow: GlowStyleRequest?,
    @field:Valid
    val price: PersonalizationPriceRequest,
)
