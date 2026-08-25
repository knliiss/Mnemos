package com.mnemos.personalization.adapter.inbound.http.personalization.content

import com.mnemos.personalization.contracts.shared.NotificationPackVariantField

data class NotificationTemplateConditionResponse(
    val field: NotificationPackVariantField,
    val value: String,
)
