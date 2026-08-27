package com.mnemos.event.adapter.out.persistence.leaderboard

import java.time.Instant
import java.util.UUID

interface DropLeaderboardProjection {
    fun getPlayerId(): UUID

    fun getNickname(): String

    fun getTotalDrops(): Long

    fun getMythicalDrops(): Long

    fun getSecretDrops(): Long

    fun getLastDropAt(): Instant
}
