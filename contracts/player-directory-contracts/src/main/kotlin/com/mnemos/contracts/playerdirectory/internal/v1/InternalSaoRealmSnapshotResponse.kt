package com.mnemos.contracts.playerdirectory.internal.v1

import java.time.Instant
import java.util.UUID

data class InternalSaoRealmSnapshotResponse(
    val generatedAt: Instant,
    val trackedOnline: Int,
    val realms: List<InternalSaoRealmResponse>,
)

data class InternalSaoRealmResponse(
    val name: String,
    val number: Int,
    val playerCount: Int,
    val players: List<InternalSaoRealmPlayerResponse>,
)

data class InternalSaoRealmPlayerResponse(
    val playerId: UUID,
    val nickname: String,
    val accountId: UUID?,
    val observedAt: Instant,
)
