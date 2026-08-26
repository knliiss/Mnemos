package com.mnemos.personalization.adapter.inbound.http.personalization.request

import com.mnemos.personalization.contracts.shared.NotificationPackVariantField
import jakarta.validation.constraints.NotBlank

data class NotificationTemplateConditionRequest(
    val field: NotificationPackVariantField,
    @field:NotBlank
    val value: String,
)
