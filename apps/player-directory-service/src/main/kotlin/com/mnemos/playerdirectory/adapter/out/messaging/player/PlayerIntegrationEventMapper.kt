package com.mnemos.playerdirectory.adapter.out.messaging.player

import com.mnemos.contracts.playerdirectory.PlayerDirectoryEventType
import com.mnemos.contracts.playerdirectory.v1.PlayerNicknameChangedV1
import com.mnemos.contracts.playerdirectory.v1.PlayerObservedV1
import com.mnemos.contracts.playerdirectory.v1.PlayerPresenceChangedV1
import com.mnemos.contracts.playerdirectory.v1.PlayerPresenceStatusV1
import com.mnemos.contracts.playerdirectory.v1.PlayerWentOfflineV1
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.playerdirectory.domain.player.model.Player
import com.mnemos.playerdirectory.domain.player.model.PlayerPresenceStatus
import java.time.Instant

object PlayerIntegrationEventMapper {
    fun Player.toObservedIntegrationEvent(observedAt: Instant): IntegrationEvent<PlayerObservedV1> =
        IntegrationEvent(
            type = PlayerDirectoryEventType.PLAYER_OBSERVED,
            aggregateId = id.id,
            occurredAt = observedAt,
            payload =
                PlayerObservedV1(
                    playerId = id.id,
                    nickname = nickname.value,
                    externalVersion = externalVersion,
                    status = presence.status.toContract(),
                    realm = presence.realm,
                    realmType = presence.realmType,
                    privacyHidden = presence.privacyHidden,
                    lastSeenOnline = presence.lastSeenOnline,
                    observedAt = observedAt,
                ),
        )

    fun nicknameChanged(
        previous: Player,
        current: Player,
        observedAt: Instant,
    ): IntegrationEvent<PlayerNicknameChangedV1>? {
        if (previous.nickname.value == current.nickname.value) {
            return null
        }

        return IntegrationEvent(
            type = PlayerDirectoryEventType.NICKNAME_CHANGED,
            aggregateId = current.id.id,
            occurredAt = observedAt,
            payload =
                PlayerNicknameChangedV1(
                    playerId = current.id.id,
                    previousNickname = previous.nickname.value,
                    nickname = current.nickname.value,
                    externalVersion = current.externalVersion,
                    observedAt = observedAt,
                ),
        )
    }

    fun presenceChanged(
        previous: Player,
        current: Player,
        observedAt: Instant,
    ): IntegrationEvent<PlayerPresenceChangedV1>? {
        val statusChanged = previous.presence.status != current.presence.status
        val realmChanged = previous.presence.realm != current.presence.realm
        val realmTypeChanged = previous.presence.realmType != current.presence.realmType
        val privacyChanged = previous.presence.privacyHidden != current.presence.privacyHidden

        if (!statusChanged && !realmChanged && !realmTypeChanged && !privacyChanged) {
            return null
        }

        return IntegrationEvent(
            type = PlayerDirectoryEventType.PRESENCE_CHANGED,
            aggregateId = current.id.id,
            occurredAt = observedAt,
            payload =
                PlayerPresenceChangedV1(
                    playerId = current.id.id,
                    nickname = current.nickname.value,
                    externalVersion = current.externalVersion,
                    previousStatus = previous.presence.status.toContract(),
                    status = current.presence.status.toContract(),
                    previousRealm = previous.presence.realm,
                    realm = current.presence.realm,
                    realmType = current.presence.realmType,
                    privacyHidden = current.presence.privacyHidden,
                    lastSeenOnline = current.presence.lastSeenOnline,
                    observedAt = observedAt,
                ),
        )
    }

    fun wentOffline(
        previous: Player,
        current: Player,
        observedAt: Instant,
    ): IntegrationEvent<PlayerWentOfflineV1>? {
        val confirmedOffline =
            previous.lastVerifiedPresenceStatus == PlayerPresenceStatus.ONLINE &&
                current.lastVerifiedPresenceStatus == PlayerPresenceStatus.OFFLINE &&
                current.presence.status == PlayerPresenceStatus.OFFLINE

        if (!confirmedOffline) {
            return null
        }

        return IntegrationEvent(
            type = PlayerDirectoryEventType.WENT_OFFLINE,
            aggregateId = current.id.id,
            occurredAt = observedAt,
            payload =
                PlayerWentOfflineV1(
                    playerId = current.id.id,
                    nickname = current.nickname.value,
                    previousRealm = previous.presence.realm,
                    lastSeenOnline = current.presence.lastSeenOnline,
                    observedAt = observedAt,
                ),
        )
    }

    private fun PlayerPresenceStatus.toContract(): PlayerPresenceStatusV1 = PlayerPresenceStatusV1.valueOf(name)
}
