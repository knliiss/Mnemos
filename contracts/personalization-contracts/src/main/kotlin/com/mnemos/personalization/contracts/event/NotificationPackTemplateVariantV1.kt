package com.mnemos.personalization.contracts.event

data class NotificationPackTemplateVariantV1(
    val id: String,
    val conditions: List<NotificationPackVariantConditionV1>,
    val emoji: String?,
    val soloTitle: String,
    val soloBody: String,
    val compact: String,
    val aggregateTitle: String,
)
