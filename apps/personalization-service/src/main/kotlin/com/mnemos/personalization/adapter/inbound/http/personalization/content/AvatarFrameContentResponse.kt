package com.mnemos.personalization.adapter.inbound.http.personalization.content

import com.mnemos.personalization.domain.personalization.model.FrameStyle

data class AvatarFrameContentResponse(
    val borderColor: String,
    val borderWidthPx: Int,
    val radiusPx: Int,
    val glow: GlowStyleResponse?,
    val secondaryBorderColor: String,
    val gapPx: Int,
    val style: FrameStyle,
    val layers: List<VisualLayerResponse>,
) : PersonalizationContentResponse
