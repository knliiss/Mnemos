package com.mnemos.notification.application.notification.personalization.model

data class NotificationPackTemplate(
    val emoji: String?,
    val soloTitle: String,
    val soloBody: String,
    val compact: String,
    val aggregateTitle: String,
)
