package com.mnemos.event.application.leaderboard.exclusion.port.out

interface LeaderboardCacheInvalidator {
    fun invalidateDrops()

    fun invalidateBoosters()
}
