package com.mnemos.event.adapter.out.persistence.leaderboard

import com.mnemos.event.application.leaderboard.booster.BoosterLeaderboardEntry
import com.mnemos.event.application.leaderboard.booster.BoosterLeaderboardReader
import com.mnemos.event.domain.player.model.CristalixPlayerId
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class BoosterLeaderboardJpaAdapter(
    private val repository: BoosterLeaderboardJpaRepository,
) : BoosterLeaderboardReader {
    override fun read(
        from: Instant,
        to: Instant?,
        limit: Int,
    ): List<BoosterLeaderboardEntry> =
        repository
            .read(
                from = from,
                to = to,
                limit = limit,
            ).map { projection ->
                BoosterLeaderboardEntry(
                    playerId = CristalixPlayerId(projection.getPlayerId()),
                    nickname = projection.getNickname(),
                    totalBoosters = projection.getTotalBoosters(),
                    lastActivatedAt = projection.getLastActivatedAt(),
                )
            }
}
