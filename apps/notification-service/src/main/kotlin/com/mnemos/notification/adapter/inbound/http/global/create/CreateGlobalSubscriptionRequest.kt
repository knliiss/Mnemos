package com.mnemos.notification.adapter.inbound.http.global.create

import com.mnemos.notification.domain.global.model.DomainGlobalEventType

data class CreateGlobalSubscriptionRequest(
    val eventType: DomainGlobalEventType,
)
