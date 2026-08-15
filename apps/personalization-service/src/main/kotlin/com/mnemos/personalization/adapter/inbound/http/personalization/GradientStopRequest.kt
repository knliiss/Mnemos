package com.mnemos.personalization.adapter.inbound.http.personalization

import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern

data class GradientStopRequest(
    @field:Pattern(regexp = "^#[0-9A-Fa-f]{6}$")
    val color: String,
    @field:Min(0)
    @field:Max(100)
    val position: Int,
)
