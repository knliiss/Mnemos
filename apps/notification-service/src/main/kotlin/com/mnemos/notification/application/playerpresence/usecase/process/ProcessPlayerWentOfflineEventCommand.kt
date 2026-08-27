package com.mnemos.notification.application.playerpresence.usecase.process

import java.time.Instant
import java.util.UUID

data class ProcessPlayerWentOfflineEventCommand(
    val messageId: UUID,
    val playerId: UUID,
    val nickname: String,
    val previousRealm: String?,
    val lastSeenOnline: Instant?,
    val occurredAt: Instant,
)
