package com.mnemos.wallet.adapter.out.cache.leaderboard

import com.mnemos.wallet.application.leaderboard.coin.CoinLeaderboardCache
import com.mnemos.wallet.application.leaderboard.coin.CoinLeaderboardEntry
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.Clock
import java.time.Duration
import java.time.Instant
import java.util.concurrent.ConcurrentHashMap

@Component
class InMemoryCoinLeaderboardCache(
    @Value("\${mnemos.wallet.leaderboard.cache-ttl:5m}") private val ttl: Duration,
    private val clock: Clock,
) : CoinLeaderboardCache {
    private val entries = ConcurrentHashMap<Int, CachedValue>()

    override fun get(limit: Int): List<CoinLeaderboardEntry>? {
        val value = entries[limit] ?: return null

        if (!value.expiresAt.isAfter(clock.instant())) {
            entries.remove(limit, value)
            return null
        }

        return value.entries
    }

    override fun put(
        limit: Int,
        entries: List<CoinLeaderboardEntry>,
    ) {
        this.entries[limit] =
            CachedValue(
                entries = entries.toList(),
                expiresAt = clock.instant().plus(ttl),
            )
    }

    override fun clear() {
        entries.clear()
    }

    private data class CachedValue(
        val entries: List<CoinLeaderboardEntry>,
        val expiresAt: Instant,
    )
}
