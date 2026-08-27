package com.mnemos.event.adapter.out.persistence.leaderboard

import com.mnemos.event.application.leaderboard.drop.DropLeaderboardEntry
import com.mnemos.event.application.leaderboard.drop.DropLeaderboardQuery
import com.mnemos.event.application.leaderboard.drop.DropLeaderboardReader
import com.mnemos.event.application.playeridentity.port.out.PlayerIdentityProjectionRepository
import com.mnemos.event.domain.player.model.CristalixPlayerId
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class DropLeaderboardJpaAdapter(
    private val repository: DropLeaderboardJpaRepository,
    private val playerIdentityRepository: PlayerIdentityProjectionRepository,
) : DropLeaderboardReader {
    override fun read(
        from: Instant,
        to: Instant?,
        query: DropLeaderboardQuery,
    ): List<DropLeaderboardEntry> {
        val playerId = query.nickname?.let(playerIdentityRepository::findCurrentOwnerId)

        if (query.nickname != null && playerId == null) {
            return emptyList()
        }

        return repository
            .read(
                from = from,
                to = to,
                rarity = query.rarity?.name,
                itemType = query.itemType?.name,
                itemName = query.itemName,
                playerId = playerId?.id,
                sort = query.sort.name,
                direction = query.direction.name,
                limit = query.limit,
            ).map { projection ->
                DropLeaderboardEntry(
                    playerId = CristalixPlayerId(projection.getPlayerId()),
                    nickname = projection.getNickname(),
                    totalDrops = projection.getTotalDrops(),
                    mythicalDrops = projection.getMythicalDrops(),
                    secretDrops = projection.getSecretDrops(),
                    lastDropAt = projection.getLastDropAt(),
                )
            }
    }
}
