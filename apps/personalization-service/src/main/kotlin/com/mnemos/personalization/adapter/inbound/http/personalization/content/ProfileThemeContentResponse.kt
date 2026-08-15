package com.mnemos.personalization.adapter.inbound.http.personalization.content

data class ProfileThemeContentResponse(
    val background: String,
    val surface: String,
    val accent: String,
    val text: String,
    val border: String,
    val glow: GlowStyleResponse?,
) : PersonalizationContentResponse
