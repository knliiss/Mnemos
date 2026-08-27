package com.mnemos.playerdirectory.application.accountlink.usecase.project

import java.time.Instant
import java.util.UUID

data class ProjectPlayerAccountLinkCommand(
    val playerId: UUID,
    val accountId: UUID,
    val recordedAt: Instant,
)
