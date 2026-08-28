package com.mnemos.playerdirectory.adapter.out.http.cristalix

import com.mnemos.playerdirectory.application.player.model.PlayerObservationBatch
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import com.mnemos.playerdirectory.domain.player.model.PlayerObservation
import com.mnemos.playerdirectory.domain.player.model.PlayerPresence
import com.mnemos.playerdirectory.domain.player.model.PlayerPresenceStatus
import java.time.Instant

object CristalixPlayerApiMapper {
    fun CristalixPlayerResponse.toObservation(observedAt: Instant): PlayerObservation {
        val hidden =
            status?.privacyHidden == true ||
                customization
                    ?.settings
                    ?.get("showOnlineStatus")
                    ?.equals("false", ignoreCase = true) == true

        val presenceStatus =
            when {
                hidden -> PlayerPresenceStatus.HIDDEN
                status?.online == true -> PlayerPresenceStatus.ONLINE
                status?.online == false -> PlayerPresenceStatus.OFFLINE
                else -> PlayerPresenceStatus.UNKNOWN
            }

        return PlayerObservation(
            playerId = CristalixPlayerId(playerId),
            nickname = PlayerNickname.of(name),
            externalVersion = version,
            externalUpdatedAt = updatedAt,
            presence =
                PlayerPresence(
                    status = presenceStatus,
                    realm = status?.realm.takeUnless { hidden },
                    realmType = status?.realmType.takeUnless { hidden },
                    privacyHidden = hidden,
                    lastSeenOnline = social?.lastSeenOnline.takeUnless { hidden },
                    observedAt = observedAt,
                ),
        )
    }

    fun CristalixPlayerBatchResponse.toObservationBatch(observedAt: Instant): PlayerObservationBatch =
        PlayerObservationBatch(
            observations = items.map { player -> player.toObservation(observedAt) },
            notFound = notFound.mapTo(mutableSetOf()) { playerId -> CristalixPlayerId(playerId) },
        )
}
