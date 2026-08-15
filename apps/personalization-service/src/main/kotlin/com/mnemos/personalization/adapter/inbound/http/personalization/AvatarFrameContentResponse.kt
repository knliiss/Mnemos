package com.mnemos.personalization.adapter.inbound.http.personalization

data class AvatarFrameContentResponse(
    val borderColor: String,
    val borderWidthPx: Int,
    val radiusPx: Int,
    val glow: GlowStyleResponse?,
) : PersonalizationContentResponse
