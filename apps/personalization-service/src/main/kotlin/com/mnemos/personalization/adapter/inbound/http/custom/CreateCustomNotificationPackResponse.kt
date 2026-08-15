package com.mnemos.personalization.adapter.inbound.http.custom

import com.mnemos.personalization.adapter.inbound.http.personalization.PersonalizationResponse

data class CreateCustomNotificationPackResponse(
    val personalization: PersonalizationResponse,
    val grantedForFree: Boolean,
)
