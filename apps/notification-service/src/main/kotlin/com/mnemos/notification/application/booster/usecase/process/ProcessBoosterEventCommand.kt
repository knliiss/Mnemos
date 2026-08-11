package com.mnemos.notification.application.booster.usecase.process

import com.mnemos.notification.domain.booster.model.DomainBoosterType
import java.time.Instant
import java.util.UUID

data class ProcessBoosterEventCommand(
    val messageId: UUID,
    val occurredAt: Instant,
    val boosterType: DomainBoosterType,
    val activatedBy: String,
)
