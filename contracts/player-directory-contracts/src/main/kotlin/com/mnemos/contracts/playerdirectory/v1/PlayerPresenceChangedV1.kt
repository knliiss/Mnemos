package com.mnemos.contracts.playerdirectory.v1

import java.time.Instant
import java.util.UUID

data class PlayerPresenceChangedV1(
    val playerId: UUID,
    val nickname: String,
    val previousStatus: PlayerPresenceStatusV1,
    val status: PlayerPresenceStatusV1,
    val previousRealm: String?,
    val realm: String?,
    val lastSeenOnline: Instant?,
    val observedAt: Instant,
)
