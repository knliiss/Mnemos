package com.mnemos.personalization.adapter.inbound.http.personalization

data class NotificationTemplateResponse(
    val emoji: String?,
    val soloTitle: String,
    val soloBody: String,
    val compact: String,
    val aggregateTitle: String,
)
