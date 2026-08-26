package com.mnemos.contracts.playerdirectory.v1

import java.time.Instant
import java.util.UUID

data class PlayerObservedV1(
    val playerId: UUID,
    val nickname: String,
    val externalVersion: Long,
    val status: PlayerPresenceStatusV1,
    val realm: String?,
    val realmType: String?,
    val privacyHidden: Boolean,
    val lastSeenOnline: Instant?,
    val observedAt: Instant,
)
