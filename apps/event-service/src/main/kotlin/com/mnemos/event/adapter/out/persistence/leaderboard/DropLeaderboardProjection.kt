package com.mnemos.event.adapter.out.persistence.leaderboard

import java.time.Instant

interface DropLeaderboardProjection {
    fun getNickname(): String

    fun getTotalDrops(): Long

    fun getMythicalDrops(): Long

    fun getSecretDrops(): Long

    fun getLastDropAt(): Instant
}
