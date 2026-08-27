package com.mnemos.profile.domain.profile.model

import java.time.Instant
import java.util.UUID

data class ProfilePlayerState(
    val playerId: UUID,
    val nickname: String,
    val externalVersion: Long,
    val presenceStatus: PresenceStatus,
    val realm: String?,
    val realmType: String?,
    val privacyHidden: Boolean,
    val lastSeenOnline: Instant?,
    val presenceObservedAt: Instant,
    val updatedAt: Instant,
) {
    fun applyPresence(
        nickname: String,
        externalVersion: Long,
        presenceStatus: PresenceStatus,
        realm: String?,
        realmType: String?,
        privacyHidden: Boolean,
        lastSeenOnline: Instant?,
        observedAt: Instant,
    ): ProfilePlayerState {
        if (observedAt.isBefore(presenceObservedAt)) {
            return this
        }

        val currentNickname =
            if (externalVersion >= this.externalVersion) {
                nickname
            } else {
                this.nickname
            }

        return copy(
            nickname = currentNickname,
            externalVersion = maxOf(this.externalVersion, externalVersion),
            presenceStatus = presenceStatus,
            realm = realm,
            realmType = realmType,
            privacyHidden = privacyHidden,
            lastSeenOnline = lastSeenOnline,
            presenceObservedAt = observedAt,
            updatedAt = maxOf(updatedAt, observedAt),
        )
    }

    fun rename(
        nickname: String,
        externalVersion: Long,
        observedAt: Instant,
    ): ProfilePlayerState {
        if (externalVersion < this.externalVersion) {
            return this
        }

        return copy(
            nickname = nickname,
            externalVersion = externalVersion,
            updatedAt = maxOf(updatedAt, observedAt),
        )
    }

    enum class PresenceStatus {
        ONLINE,
        OFFLINE,
        UNKNOWN,
        HIDDEN,
    }
}
