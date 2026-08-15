package com.mnemos.personalization.adapter.inbound.http.moderation

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class RejectPersonalizationRequest(
    @field:NotBlank
    @field:Size(max = 500)
    val reason: String,
)
