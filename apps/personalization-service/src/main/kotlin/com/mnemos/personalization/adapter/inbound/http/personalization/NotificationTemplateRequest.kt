package com.mnemos.personalization.adapter.inbound.http.personalization

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class NotificationTemplateRequest(
    @field:Size(max = 16)
    val emoji: String?,
    @field:NotBlank
    @field:Size(max = 512)
    val soloTitle: String,
    @field:NotBlank
    @field:Size(max = 512)
    val soloBody: String,
    @field:NotBlank
    @field:Size(max = 512)
    val compact: String,
    @field:NotBlank
    @field:Size(max = 512)
    val aggregateTitle: String,
)
