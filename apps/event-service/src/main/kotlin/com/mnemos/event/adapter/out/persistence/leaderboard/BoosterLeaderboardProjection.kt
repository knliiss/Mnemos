package com.mnemos.event.adapter.out.persistence.leaderboard

import java.time.Instant
import java.util.UUID

interface BoosterLeaderboardProjection {
    fun getPlayerId(): UUID

    fun getNickname(): String

    fun getTotalBoosters(): Long

    fun getLastActivatedAt(): Instant
}
