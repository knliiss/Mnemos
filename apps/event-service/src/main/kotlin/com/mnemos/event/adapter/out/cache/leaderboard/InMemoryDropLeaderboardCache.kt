package com.mnemos.event.adapter.out.cache.leaderboard

import com.mnemos.event.application.leaderboard.drop.DropLeaderboardCache
import com.mnemos.event.application.leaderboard.drop.DropLeaderboardEntry
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Component
class InMemoryDropLeaderboardCache(
    @Value("\${mnemos.event.leaderboard.cache-ttl:5m}") private val ttl: Duration,
    private val clock: Clock,
) : DropLeaderboardCache {
    private val entries = ConcurrentHashMap<DropLeaderboardCache.Key, CachedValue>()

    override fun get(key: DropLeaderboardCache.Key): List<DropLeaderboardEntry>? {
        val value = entries[key] ?: return null

        if (!value.expiresAt.isAfter(clock.instant())) {
            entries.remove(key, value)
            return null
        }

        return value.entries
    }

    override fun put(
        key: DropLeaderboardCache.Key,
        entries: List<DropLeaderboardEntry>,
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
        val entries: List<DropLeaderboardEntry>,
        val expiresAt: Instant,
    )
}
