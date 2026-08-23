package com.mnemos.personalization.adapter.inbound.http.personalization.content

import com.mnemos.personalization.domain.personalization.model.VisualLayerAnimation
import com.mnemos.personalization.domain.personalization.model.VisualLayerBlendMode
import java.util.UUID

data class VisualLayerResponse(
    val id: String,
    val assetId: UUID,
    val assetUrl: String,
    val xPercent: Int,
    val yPercent: Int,
    val widthPercent: Int,
    val opacity: Int,
    val rotationDegrees: Int,
    val blendMode: VisualLayerBlendMode,
    val animation: VisualLayerAnimation,
    val zIndex: Int,
)
