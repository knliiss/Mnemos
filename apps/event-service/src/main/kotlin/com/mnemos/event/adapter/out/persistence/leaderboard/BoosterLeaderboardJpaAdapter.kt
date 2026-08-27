package com.mnemos.event.adapter.out.persistence.leaderboard

import com.mnemos.event.application.leaderboard.booster.BoosterLeaderboardEntry
import com.mnemos.event.application.leaderboard.booster.BoosterLeaderboardReader
import com.mnemos.event.application.playeridentity.port.out.PlayerIdentityProjectionRepository
import com.mnemos.event.domain.player.model.CristalixPlayerId
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class BoosterLeaderboardJpaAdapter(
    private val repository: BoosterLeaderboardJpaRepository,
    private val playerIdentityRepository: PlayerIdentityProjectionRepository,
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
                val playerId = CristalixPlayerId(projection.getPlayerId())
                val currentNickname = playerIdentityRepository.findCurrentNickname(playerId)

                BoosterLeaderboardEntry(
                    playerId = playerId,
                    nickname = currentNickname ?: projection.getNickname(),
                    totalBoosters = projection.getTotalBoosters(),
                    lastActivatedAt = projection.getLastActivatedAt(),
                )
            }
}
