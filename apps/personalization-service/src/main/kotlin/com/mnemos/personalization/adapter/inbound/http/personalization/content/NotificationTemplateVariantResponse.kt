package com.mnemos.personalization.adapter.inbound.http.personalization.content

data class NotificationTemplateVariantResponse(
    val id: String,
    val conditions: List<NotificationTemplateConditionResponse>,
    val emoji: String?,
    val soloTitle: String,
    val soloBody: String,
    val compact: String,
    val aggregateTitle: String,
)
