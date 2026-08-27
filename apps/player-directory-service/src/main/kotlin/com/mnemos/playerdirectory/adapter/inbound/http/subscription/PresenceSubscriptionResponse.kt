package com.mnemos.playerdirectory.adapter.inbound.http.subscription

import java.time.Instant
import java.util.UUID

data class PresenceSubscriptionResponse(
    val id: UUID,
    val playerId: UUID,
    val nickname: String?,
    val presenceStatus: String?,
    val realm: String?,
    val lastSeenOnline: Instant?,
    val createdAt: Instant,
)
