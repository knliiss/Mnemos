package com.mnemos.personalization.adapter.inbound.http.moderation

import com.mnemos.personalization.adapter.inbound.http.personalization.request.PersonalizationPriceRequest
import jakarta.validation.Valid

data class ApprovePersonalizationRequest(
    @field:Valid
    val price: PersonalizationPriceRequest,
)
