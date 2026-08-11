package com.mnemos.notification.application.global.usecase.create

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.domain.global.model.DomainGlobalEventType

data class CreateGlobalSubscriptionCommand(
    val userId: UserId,
    val eventType: DomainGlobalEventType,
)
