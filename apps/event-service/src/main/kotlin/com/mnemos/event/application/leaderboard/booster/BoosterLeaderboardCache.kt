package com.mnemos.event.application.leaderboard.booster

import com.mnemos.event.domain.wipe.model.WipeId

interface BoosterLeaderboardCache {
    fun get(key: Key): List<BoosterLeaderboardEntry>?

    fun put(
        key: Key,
        entries: List<BoosterLeaderboardEntry>,
    )

    fun clear()

    data class Key(
        val wipeId: WipeId,
        val limit: Int,
    )
}
