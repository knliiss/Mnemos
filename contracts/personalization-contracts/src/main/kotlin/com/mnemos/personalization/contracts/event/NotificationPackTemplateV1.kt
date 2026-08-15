package com.mnemos.personalization.contracts.event

import com.mnemos.personalization.contracts.shared.NotificationPackTemplateType

data class NotificationPackTemplateV1(
    val type: NotificationPackTemplateType,
    val emoji: String?,
    val soloTitle: String,
    val soloBody: String,
    val compact: String,
    val aggregateTitle: String,
)
