package com.mnemos.event.application.leaderboard.drop

import com.mnemos.event.domain.wipe.model.WipeId

interface DropLeaderboardCache {
    fun get(key: Key): List<DropLeaderboardEntry>?

    fun put(
        key: Key,
        entries: List<DropLeaderboardEntry>,
    )

    fun clear()

    data class Key(
        val wipeId: WipeId,
        val query: DropLeaderboardQuery,
    )
}
