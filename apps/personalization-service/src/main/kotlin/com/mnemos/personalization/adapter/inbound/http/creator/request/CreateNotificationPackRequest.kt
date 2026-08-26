package com.mnemos.personalization.adapter.inbound.http.creator.request

import com.mnemos.personalization.adapter.inbound.http.personalization.request.NotificationTemplateRequest
import com.mnemos.personalization.adapter.inbound.http.personalization.request.NotificationValueFormsRequest
import com.mnemos.personalization.adapter.inbound.http.personalization.request.PersonalizationPriceRequest
import com.mnemos.personalization.contracts.shared.NotificationPackValueKey
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateKey
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class CreateNotificationPackRequest(
    @field:NotBlank
    @field:Size(min = 3, max = 64)
    val name: String,
    @field:NotBlank
    @field:Size(max = 512)
    val mixedAggregateTitle: String,
    @field:Valid
    @field:Size(min = 5, max = 5)
    val templates: Map<NotificationTemplateKey, NotificationTemplateRequest>,
    @field:Valid
    val valueMappings: Map<NotificationPackValueKey, Map<String, NotificationValueFormsRequest>> = emptyMap(),
    @field:Valid
    val price: PersonalizationPriceRequest,
)
