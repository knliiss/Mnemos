package com.mnemos.notification.application.raid.usecase.process

import java.time.Instant
import java.util.UUID

data class ProcessRaidEventCommand(
    val messageId: UUID,
    val occurredAt: Instant,
    val locations: Set<Int>,
)
