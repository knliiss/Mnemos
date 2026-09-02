package com.mnemos.telegram.bot.adapter.out.playerdirectory

import com.mnemos.contracts.playerdirectory.internal.v1.InternalSaoRealmPlayerResponse
import com.mnemos.contracts.playerdirectory.internal.v1.InternalSaoRealmResponse
import com.mnemos.contracts.playerdirectory.internal.v1.InternalSaoRealmSnapshotResponse
import com.mnemos.telegram.bot.application.online.model.SaoOnlinePlayer
import com.mnemos.telegram.bot.application.online.model.SaoOnlineRealm
import com.mnemos.telegram.bot.application.online.model.SaoOnlineSnapshot

object PlayerDirectorySaoOnlineMapper {
    fun InternalSaoRealmSnapshotResponse.toModel(): SaoOnlineSnapshot =
        SaoOnlineSnapshot(
            generatedAt = generatedAt,
            trackedOnline = trackedOnline,
            realms =
                realms
                    .sortedBy(InternalSaoRealmResponse::number)
                    .map { realm -> realm.toModel() },
        )

    private fun InternalSaoRealmResponse.toModel(): SaoOnlineRealm =
        SaoOnlineRealm(
            name = name,
            number = number,
            playerCount = playerCount,
            players = players.map { player -> player.toModel() },
        )

    private fun InternalSaoRealmPlayerResponse.toModel(): SaoOnlinePlayer =
        SaoOnlinePlayer(
            playerId = playerId,
            nickname = nickname,
            accountId = accountId,
            observedAt = observedAt,
        )
}
