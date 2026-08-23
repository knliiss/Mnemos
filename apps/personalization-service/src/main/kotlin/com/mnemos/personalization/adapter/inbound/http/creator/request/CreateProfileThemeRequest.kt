package com.mnemos.personalization.adapter.inbound.http.creator.request

import com.mnemos.personalization.adapter.inbound.http.personalization.request.GlowStyleRequest
import com.mnemos.personalization.adapter.inbound.http.personalization.request.PersonalizationPriceRequest
import jakarta.validation.Valid
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
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
    @field:Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    val backgroundSecondary: String = background,
    @field:Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    val surfaceSecondary: String = surface,
    @field:Min(0)
    @field:Max(360)
    val gradientAngleDegrees: Int = 145,
    @field:Min(0)
    @field:Max(100)
    val textureStrength: Int = 0,
    @field:Valid
    @field:Size(max = 8)
    val layers: List<VisualLayerRequest> = emptyList(),
)
