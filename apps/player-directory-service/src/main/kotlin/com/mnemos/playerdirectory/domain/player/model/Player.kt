package com.mnemos.playerdirectory.domain.player.model

import java.time.Instant

data class Player(
    val id: CristalixPlayerId,
    val nickname: PlayerNickname,
    val externalVersion: Long,
    val externalUpdatedAt: Instant?,
    val presence: PlayerPresence,
    val createdAt: Instant,
    val updatedAt: Instant,
) {
    fun applyObservation(observation: PlayerObservation): Player {
        require(observation.playerId == id) { "Cannot apply an observation for another player" }

        if (observation.externalVersion < externalVersion) {
            return this
        }

        val nextPresence =
            if (observation.presence.observedAt.isBefore(presence.observedAt)) {
                presence
            } else {
                observation.presence
            }

        return copy(
            nickname = observation.nickname,
            externalVersion = observation.externalVersion,
            externalUpdatedAt = observation.externalUpdatedAt,
            presence = nextPresence,
            updatedAt = maxOf(updatedAt, observation.presence.observedAt),
        )
    }

    companion object {
        fun fromObservation(observation: PlayerObservation): Player =
            Player(
                id = observation.playerId,
                nickname = observation.nickname,
                externalVersion = observation.externalVersion,
                externalUpdatedAt = observation.externalUpdatedAt,
                presence = observation.presence,
                createdAt = observation.presence.observedAt,
                updatedAt = observation.presence.observedAt,
            )
    }
}
