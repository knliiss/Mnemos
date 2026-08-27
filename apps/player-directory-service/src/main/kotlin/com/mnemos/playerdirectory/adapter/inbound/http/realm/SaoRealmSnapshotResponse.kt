package com.mnemos.playerdirectory.adapter.inbound.http.realm

import java.time.Instant
import java.util.UUID

data class SaoRealmSnapshotResponse(
    val generatedAt: Instant,
    val trackedOnline: Int,
    val realms: List<SaoRealmResponse>,
)

data class SaoRealmResponse(
    val name: String,
    val number: Int,
    val playerCount: Int,
    val players: List<SaoRealmPlayerResponse>,
)

data class SaoRealmPlayerResponse(
    val playerId: UUID,
    val nickname: String,
    val accountId: UUID?,
    val observedAt: Instant,
)
