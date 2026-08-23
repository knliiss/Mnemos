package com.mnemos.personalization.adapter.inbound.http.personalization.content

import com.mnemos.personalization.domain.personalization.model.EffectType

data class ProfileEffectContentResponse(
    val effect: EffectType,
    val intensity: Int,
    val primaryColor: String,
    val secondaryColor: String?,
    val speed: Int,
    val density: Int,
    val scale: Int,
    val layers: List<VisualLayerResponse>,
) : PersonalizationContentResponse
