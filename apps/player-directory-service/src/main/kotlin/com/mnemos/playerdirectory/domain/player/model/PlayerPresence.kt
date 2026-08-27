package com.mnemos.playerdirectory.domain.player.model

import java.time.Instant

data class PlayerPresence(
    val status: PlayerPresenceStatus,
    val realm: String?,
    val realmType: String?,
    val privacyHidden: Boolean,
    val lastSeenOnline: Instant?,
    val observedAt: Instant,
)
