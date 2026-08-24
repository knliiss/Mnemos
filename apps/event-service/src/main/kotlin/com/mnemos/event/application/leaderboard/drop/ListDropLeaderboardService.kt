package com.mnemos.event.application.leaderboard.drop

import com.mnemos.event.application.wipe.ResolveWipeService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class ListDropLeaderboardService(
    private val reader: DropLeaderboardReader,
    private val resolveWipeService: ResolveWipeService,
) {
    fun execute(query: DropLeaderboardQuery): List<DropLeaderboardEntry> {
        val wipe = resolveWipeService.execute(query.wipeId)
        val normalizedQuery =
            query.copy(
                itemName = query.itemName?.trim()?.takeIf(String::isNotEmpty),
                nickname = query.nickname?.trim()?.takeIf(String::isNotEmpty),
                limit = query.limit.coerceIn(MIN_LIMIT, MAX_LIMIT),
            )

        return reader.read(
            from = wipe.startedAt,
            to = wipe.endedAt,
            query = normalizedQuery,
        )
    }

    private companion object {
        const val MIN_LIMIT = 3
        const val MAX_LIMIT = 100
    }
}
