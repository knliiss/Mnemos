package com.mnemos.personalization.adapter.inbound.http.personalization.request

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern

data class GlowStyleRequest(
    @field:Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    val color: String,
    @field:Min(1)
    @field:Max(100)
    val strength: Int,
)
