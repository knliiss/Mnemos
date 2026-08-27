package com.mnemos.event.adapter.out.cache.leaderboard

import com.mnemos.event.application.leaderboard.booster.BoosterLeaderboardCache
import com.mnemos.event.application.leaderboard.booster.BoosterLeaderboardEntry
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Component
class InMemoryBoosterLeaderboardCache(
    @Value("\${mnemos.event.leaderboard.cache-ttl:5m}") private val ttl: Duration,
    private val clock: Clock,
) : BoosterLeaderboardCache {
    private val entries = ConcurrentHashMap<BoosterLeaderboardCache.Key, CachedValue>()

    override fun get(key: BoosterLeaderboardCache.Key): List<BoosterLeaderboardEntry>? {
        val value = entries[key] ?: return null

        if (!value.expiresAt.isAfter(clock.instant())) {
            entries.remove(key, value)
            return null
        }

        return value.entries
    }

    override fun put(
        key: BoosterLeaderboardCache.Key,
        entries: List<BoosterLeaderboardEntry>,
    ) {
        this.entries[key] =
            CachedValue(
                entries = entries.toList(),
                expiresAt = clock.instant().plus(ttl),
            )
    }

    override fun clear() {
        entries.clear()
    }

    private data class CachedValue(
        val entries: List<BoosterLeaderboardEntry>,
        val expiresAt: Instant,
    )
}
