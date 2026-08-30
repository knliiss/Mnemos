package com.mnemos.playerdirectory.adapter.inbound.http.internal

import com.mnemos.contracts.playerdirectory.internal.v1.InternalSaoRealmPlayerResponse
import com.mnemos.contracts.playerdirectory.internal.v1.InternalSaoRealmResponse
import com.mnemos.contracts.playerdirectory.internal.v1.InternalSaoRealmSnapshotResponse
import com.mnemos.playerdirectory.application.realm.model.SaoRealm
import com.mnemos.playerdirectory.application.realm.model.SaoRealmPlayer
import com.mnemos.playerdirectory.application.realm.model.SaoRealmSnapshot

object InternalSaoRealmApiMapper {
    fun SaoRealmSnapshot.toInternalResponse(): InternalSaoRealmSnapshotResponse =
        InternalSaoRealmSnapshotResponse(
            generatedAt = generatedAt,
            trackedOnline = trackedOnline,
            realms = realms.map { realm -> realm.toInternalResponse() },
        )

    private fun SaoRealm.toInternalResponse(): InternalSaoRealmResponse =
        InternalSaoRealmResponse(
            name = name.value,
            number = name.number,
            playerCount = players.size,
            players = players.map { player -> player.toInternalResponse() },
        )

    private fun SaoRealmPlayer.toInternalResponse(): InternalSaoRealmPlayerResponse =
        InternalSaoRealmPlayerResponse(
            playerId = playerId.id,
            nickname = nickname,
            accountId = accountId,
            observedAt = observedAt,
        )
}
