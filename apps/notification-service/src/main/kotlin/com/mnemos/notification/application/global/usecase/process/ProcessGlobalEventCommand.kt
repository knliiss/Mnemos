package com.mnemos.notification.application.global.usecase.process

import com.mnemos.notification.domain.global.model.DomainGlobalEventType
import java.time.Instant
import java.util.UUID

data class ProcessGlobalEventCommand(
    val messageId: UUID,
    val occurredAt: Instant,
    val eventType: DomainGlobalEventType,
)
