package com.mnemos.playerdirectory.adapter.inbound.http.player

import java.time.Instant
import java.util.UUID

data class PlayerResponse(
    val playerId: UUID,
    val nickname: String,
    val presenceStatus: String,
    val realm: String?,
    val realmType: String?,
    val privacyHidden: Boolean,
    val lastSeenOnline: Instant?,
    val observedAt: Instant,
)
