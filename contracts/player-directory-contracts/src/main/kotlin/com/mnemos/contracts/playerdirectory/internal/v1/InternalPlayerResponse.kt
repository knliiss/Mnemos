package com.mnemos.contracts.playerdirectory.internal.v1

import com.mnemos.contracts.playerdirectory.v1.PlayerPresenceStatusV1
import java.time.Instant
import java.util.UUID

data class InternalPlayerResponse(
    val playerId: UUID,
    val nickname: String,
    val presenceStatus: PlayerPresenceStatusV1,
    val realm: String?,
    val realmType: String?,
    val lastSeenOnline: Instant?,
    val observedAt: Instant,
)
