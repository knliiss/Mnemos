package com.mnemos.personalization.adapter.inbound.http.personalization.content

import com.mnemos.personalization.contracts.shared.NotificationPackValueKey
import com.mnemos.personalization.domain.personalization.model.NotificationTemplateKey

data class NotificationPackContentResponse(
    val mixedAggregateTitle: String,
    val templates: Map<NotificationTemplateKey, NotificationTemplateResponse>,
    val valueMappings: Map<NotificationPackValueKey, Map<String, NotificationValueFormsResponse>> = emptyMap(),
) : PersonalizationContentResponse
