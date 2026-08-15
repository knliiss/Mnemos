package com.mnemos.personalization.adapter.inbound.http.personalization.content

import com.mnemos.personalization.domain.personalization.model.EffectType

data class ProfileEffectContentResponse(
    val effect: EffectType,
    val intensity: Int,
    val primaryColor: String,
    val secondaryColor: String?,
) : PersonalizationContentResponse
