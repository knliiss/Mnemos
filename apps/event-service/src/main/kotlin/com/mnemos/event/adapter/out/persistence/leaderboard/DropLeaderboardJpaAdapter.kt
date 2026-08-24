package com.mnemos.event.adapter.out.persistence.leaderboard

import com.mnemos.event.application.leaderboard.drop.DropLeaderboardEntry
import com.mnemos.event.application.leaderboard.drop.DropLeaderboardQuery
import com.mnemos.event.application.leaderboard.drop.DropLeaderboardReader
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
class DropLeaderboardJpaAdapter(
    private val repository: DropLeaderboardJpaRepository,
) : DropLeaderboardReader {
    override fun read(
        from: Instant,
        to: Instant?,
        query: DropLeaderboardQuery,
    ): List<DropLeaderboardEntry> =
        repository
            .read(
                from = from,
                to = to,
                rarity = query.rarity?.name,
                itemType = query.itemType?.name,
                itemName = query.itemName,
                nickname = query.nickname,
                sort = query.sort.name,
                limit = query.limit,
            ).map { projection ->
                DropLeaderboardEntry(
                    nickname = projection.getNickname(),
                    totalDrops = projection.getTotalDrops(),
                    mythicalDrops = projection.getMythicalDrops(),
                    secretDrops = projection.getSecretDrops(),
                    lastDropAt = projection.getLastDropAt(),
                )
            }
}
