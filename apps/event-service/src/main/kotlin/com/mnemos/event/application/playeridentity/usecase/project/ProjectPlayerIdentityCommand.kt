package com.mnemos.event.application.playeridentity.usecase.project

import java.time.Instant
import java.util.UUID

data class ProjectPlayerIdentityCommand(
    val playerId: UUID,
    val nickname: String,
    val externalVersion: Long,
    val observedAt: Instant,
)
