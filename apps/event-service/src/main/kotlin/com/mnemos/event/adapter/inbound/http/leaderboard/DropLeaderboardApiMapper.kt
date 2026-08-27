package com.mnemos.event.adapter.inbound.http.leaderboard

import com.mnemos.event.application.leaderboard.drop.DropLeaderboardEntry
import com.mnemos.event.application.leaderboard.drop.DropLeaderboardQuery
import com.mnemos.event.domain.wipe.model.WipeId

object DropLeaderboardApiMapper {
    fun DropLeaderboardRequest.toQuery(): DropLeaderboardQuery =
        DropLeaderboardQuery(
            wipeId = wipeId?.let(::WipeId),
            rarity = rarity,
            itemType = itemType,
            itemName = itemName,
            nickname = nickname,
            sort = sort,
            direction = direction,
            limit = limit,
        )

    fun DropLeaderboardEntry.toResponse(rank: Int): DropLeaderboardResponse =
        DropLeaderboardResponse(
            rank = rank,
            playerId = playerId.id,
            nickname = nickname,
            totalDrops = totalDrops,
            mythicalDrops = mythicalDrops,
            secretDrops = secretDrops,
            lastDropAt = lastDropAt,
        )
}
