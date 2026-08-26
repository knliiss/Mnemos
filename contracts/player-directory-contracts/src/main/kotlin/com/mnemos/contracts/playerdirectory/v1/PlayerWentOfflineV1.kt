package com.mnemos.contracts.playerdirectory.v1

import java.time.Instant
import java.util.UUID

data class PlayerWentOfflineV1(
    val playerId: UUID,
    val nickname: String,
    val previousRealm: String?,
    val lastSeenOnline: Instant?,
    val observedAt: Instant,
)
