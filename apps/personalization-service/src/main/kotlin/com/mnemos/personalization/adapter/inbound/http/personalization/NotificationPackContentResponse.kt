package com.mnemos.personalization.adapter.inbound.http.personalization

import com.mnemos.personalization.domain.personalization.model.NotificationTemplateKey

data class NotificationPackContentResponse(
    val templates: Map<NotificationTemplateKey, NotificationTemplateResponse>,
) : PersonalizationContentResponse
