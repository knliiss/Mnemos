package com.mnemos.event.application.leaderboard.booster

import com.mnemos.event.application.wipe.ResolveWipeService
import com.mnemos.event.domain.wipe.model.WipeId
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListBoosterLeaderboardService(
    private val reader: BoosterLeaderboardReader,
    private val cache: BoosterLeaderboardCache,
    private val resolveWipeService: ResolveWipeService,
) {
    fun execute(
        wipeId: WipeId?,
        limit: Int,
    ): List<BoosterLeaderboardEntry> {
        val normalizedLimit = limit.coerceIn(MIN_LIMIT, MAX_LIMIT)
        val wipe = resolveWipeService.execute(wipeId)
        val cacheKey =
            BoosterLeaderboardCache.Key(
                wipeId = wipe.id,
                limit = normalizedLimit,
            )

        cache.get(cacheKey)?.let { return it }

        val entries =
            reader.read(
                from = wipe.startedAt,
                to = wipe.endedAt,
                limit = normalizedLimit,
            )

        cache.put(cacheKey, entries)

        return entries
    }

    private companion object {
        const val MIN_LIMIT = 3
        const val MAX_LIMIT = 100
    }
}
