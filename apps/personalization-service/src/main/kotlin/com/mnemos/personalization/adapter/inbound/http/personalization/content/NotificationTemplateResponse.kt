package com.mnemos.personalization.adapter.inbound.http.personalization.content

data class NotificationTemplateResponse(
    val emoji: String?,
    val soloTitle: String,
    val soloBody: String,
    val compact: String,
    val aggregateTitle: String,
    val variants: List<NotificationTemplateVariantResponse> = emptyList(),
)
