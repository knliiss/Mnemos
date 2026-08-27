package com.mnemos.playerdirectory.adapter.out.persistence.realm

import java.time.Instant
import java.util.UUID

interface SaoRealmPlayerProjection {
    val playerId: UUID
    val nickname: String
    val accountId: UUID?
    val realm: String
    val observedAt: Instant
}
