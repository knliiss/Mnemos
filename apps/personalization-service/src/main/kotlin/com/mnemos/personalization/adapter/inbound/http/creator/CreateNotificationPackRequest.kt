package com.mnemos.personalization.adapter.inbound.http.creator

import com.mnemos.personalization.adapter.inbound.http.personalization.NotificationTemplateRequest
import com.mnemos.personalization.adapter.inbound.http.personalization.PersonalizationPriceRequest
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateKey
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateNotificationPackRequest(
    @field:NotBlank
    @field:Size(min = 3, max = 64)
    val name: String,
    @field:Valid
    @field:Size(min = 5, max = 5)
    val templates: Map<NotificationTemplateKey, NotificationTemplateRequest>,
    @field:Valid
    val price: PersonalizationPriceRequest,
)
