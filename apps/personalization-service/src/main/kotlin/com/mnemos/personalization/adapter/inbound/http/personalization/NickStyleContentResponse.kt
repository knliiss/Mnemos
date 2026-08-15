package com.mnemos.personalization.adapter.inbound.http.personalization

data class NickStyleContentResponse(
    val colors: List<GradientStopResponse>,
    val angleDegrees: Int,
    val glow: GlowStyleResponse?,
) : PersonalizationContentResponse
