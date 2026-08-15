package com.mnemos.personalization.contracts.event

data class NotificationPackSnapshotV1(
    val mixedAggregateTitle: String,
    val templates: List<NotificationPackTemplateV1>,
)
