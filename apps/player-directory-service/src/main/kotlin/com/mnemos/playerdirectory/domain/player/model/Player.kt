package com.mnemos.playerdirectory.domain.player.model

import com.mnemos.playerdirectory.domain.player.policy.PlayerPresenceTransitionPolicy
import java.time.Instant

data class Player(
    val id: CristalixPlayerId,
    val nickname: PlayerNickname,
    val externalVersion: Long,
    val externalUpdatedAt: Instant?,
    val presence: PlayerPresence,
    val lastVerifiedPresenceStatus: PlayerPresenceStatus?,
    val offlineCandidateSince: Instant?,
    val offlineCandidateCount: Int,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    init {
        require(
            lastVerifiedPresenceStatus == null ||
                lastVerifiedPresenceStatus == PlayerPresenceStatus.ONLINE ||
                lastVerifiedPresenceStatus == PlayerPresenceStatus.OFFLINE,
        ) {
            "Last verified presence status must be ONLINE, OFFLINE or null"
        }
        require(offlineCandidateCount >= 0) {
            "Offline candidate count cannot be negative"
        }
        require((offlineCandidateSince == null) == (offlineCandidateCount == 0)) {
            "Offline candidate timestamp and count must be present together"
        }
    }

    fun applyObservation(
        observation: PlayerObservation,
        transitionPolicy: PlayerPresenceTransitionPolicy,
    ): Player {
        require(observation.playerId == id) {
            "Cannot apply an observation for another player"
        }

        if (observation.externalVersion < externalVersion) {
            return this
        }

        val presenceApplication = applyPresenceObservation(observation, transitionPolicy)

        return copy(
            nickname = observation.nickname,
            externalVersion = observation.externalVersion,
            externalUpdatedAt = observation.externalUpdatedAt,
            presence = presenceApplication.presence,
            lastVerifiedPresenceStatus = presenceApplication.lastVerifiedStatus,
            offlineCandidateSince = presenceApplication.offlineCandidateSince,
            offlineCandidateCount = presenceApplication.offlineCandidateCount,
            updatedAt = maxOf(updatedAt, observation.presence.observedAt),
        )
    }

    private fun applyPresenceObservation(
        observation: PlayerObservation,
        transitionPolicy: PlayerPresenceTransitionPolicy,
    ): PresenceApplication {
        if (observation.presence.observedAt.isBefore(presence.observedAt)) {
            return currentPresenceApplication()
        }

        return when (observation.presence.status) {
            PlayerPresenceStatus.ONLINE ->
                PresenceApplication(
                    presence = observation.presence,
                    lastVerifiedStatus = PlayerPresenceStatus.ONLINE,
                )

            PlayerPresenceStatus.OFFLINE -> applyOfflineObservation(observation, transitionPolicy)

            PlayerPresenceStatus.UNKNOWN,
            PlayerPresenceStatus.HIDDEN,
            ->
                PresenceApplication(
                    presence = observation.presence,
                    lastVerifiedStatus = lastVerifiedPresenceStatus,
                )
        }
    }

    private fun applyOfflineObservation(
        observation: PlayerObservation,
        transitionPolicy: PlayerPresenceTransitionPolicy,
    ): PresenceApplication {
        if (lastVerifiedPresenceStatus != PlayerPresenceStatus.ONLINE) {
            return PresenceApplication(
                presence = observation.presence,
                lastVerifiedStatus = PlayerPresenceStatus.OFFLINE,
            )
        }

        val candidateSince = offlineCandidateSince ?: observation.presence.observedAt
        val candidateCount = offlineCandidateCount + 1
        val confirmed =
            transitionPolicy.isOfflineConfirmed(
                candidateSince = candidateSince,
                candidateCount = candidateCount,
                observedAt = observation.presence.observedAt,
            )

        if (confirmed) {
            return PresenceApplication(
                presence = observation.presence,
                lastVerifiedStatus = PlayerPresenceStatus.OFFLINE,
            )
        }

        return PresenceApplication(
            presence = presence,
            lastVerifiedStatus = lastVerifiedPresenceStatus,
            offlineCandidateSince = candidateSince,
            offlineCandidateCount = candidateCount,
        )
    }

    private fun currentPresenceApplication(): PresenceApplication =
        PresenceApplication(
            presence = presence,
            lastVerifiedStatus = lastVerifiedPresenceStatus,
            offlineCandidateSince = offlineCandidateSince,
            offlineCandidateCount = offlineCandidateCount,
        )

    private data class PresenceApplication(
        val presence: PlayerPresence,
        val lastVerifiedStatus: PlayerPresenceStatus?,
        val offlineCandidateSince: Instant? = null,
        val offlineCandidateCount: Int = 0,
    )

    companion object {
        fun fromObservation(observation: PlayerObservation): Player =
            Player(
                id = observation.playerId,
                nickname = observation.nickname,
                externalVersion = observation.externalVersion,
                externalUpdatedAt = observation.externalUpdatedAt,
                presence = observation.presence,
                lastVerifiedPresenceStatus = observation.presence.status.toVerifiedStatusOrNull(),
                offlineCandidateSince = null,
                offlineCandidateCount = 0,
                createdAt = observation.presence.observedAt,
                updatedAt = observation.presence.observedAt,
            )

        private fun PlayerPresenceStatus.toVerifiedStatusOrNull(): PlayerPresenceStatus? =
            when (this) {
                PlayerPresenceStatus.ONLINE,
                PlayerPresenceStatus.OFFLINE,
                -> this

                PlayerPresenceStatus.UNKNOWN,
                PlayerPresenceStatus.HIDDEN,
                -> null
            }
    }
}
