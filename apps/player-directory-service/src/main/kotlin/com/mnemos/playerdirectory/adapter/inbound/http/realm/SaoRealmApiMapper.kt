package com.mnemos.playerdirectory.adapter.inbound.http.realm

import com.mnemos.playerdirectory.application.realm.model.SaoRealm
import com.mnemos.playerdirectory.application.realm.model.SaoRealmPlayer
import com.mnemos.playerdirectory.application.realm.model.SaoRealmSnapshot

object SaoRealmApiMapper {
    fun SaoRealmSnapshot.toResponse(): SaoRealmSnapshotResponse =
        SaoRealmSnapshotResponse(
            generatedAt = generatedAt,
            trackedOnline = trackedOnline,
            realms = realms.map { realm -> realm.toResponse() },
        )

    private fun SaoRealm.toResponse(): SaoRealmResponse =
        SaoRealmResponse(
            name = name.value,
            number = name.number,
            playerCount = players.size,
            players = players.map { player -> player.toResponse() },
        )

    private fun SaoRealmPlayer.toResponse(): SaoRealmPlayerResponse =
        SaoRealmPlayerResponse(
            playerId = playerId.id,
            nickname = nickname,
            accountId = accountId,
            observedAt = observedAt,
        )
}
