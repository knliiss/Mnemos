package com.mnemos.personalization.adapter.inbound.http.creator.request

import com.mnemos.personalization.domain.personalization.model.VisualLayerAnimation
import com.mnemos.personalization.domain.personalization.model.VisualLayerBlendMode
import jakarta.validation.constraints.Max
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.Pattern
import java.util.UUID

data class VisualLayerRequest(
    @field:Pattern(regexp = "^[a-zA-Z0-9_-]{1,48}$")
    val id: String,
    val assetId: UUID,
    @field:Min(-50)
    @field:Max(150)
    val xPercent: Int,
    @field:Min(-50)
    @field:Max(150)
    val yPercent: Int,
    @field:Min(5)
    @field:Max(200)
    val widthPercent: Int,
    @field:Min(1)
    @field:Max(100)
    val opacity: Int,
    @field:Min(-180)
    @field:Max(180)
    val rotationDegrees: Int,
    val blendMode: VisualLayerBlendMode,
    val animation: VisualLayerAnimation,
    @field:Min(0)
    @field:Max(20)
    val zIndex: Int,
)
