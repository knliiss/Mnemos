package com.mnemos.playerdirectory.domain.player.model

import com.mnemos.playerdirectory.domain.player.policy.PlayerPresenceTransitionPolicy
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant
import java.util.UUID

class PlayerTest {
    private val playerId = CristalixPlayerId(UUID.fromString("ea0a316c-ec21-11ea-acca-1cb72caa35fd"))
    private val transitionPolicy =
        PlayerPresenceTransitionPolicy(
            offlineConfirmationDelay = Duration.ZERO,
            requiredOfflineObservations = 2,
        )

    @Test
    fun `older external version cannot roll player identity backward`() {
        val current = Player.fromObservation(observation(version = 12, nickname = "CurrentName", observedAt = "2026-08-27T00:00:00Z"))
        val stale = observation(version = 11, nickname = "OldName", observedAt = "2026-08-27T00:01:00Z")

        val result = current.applyObservation(stale, transitionPolicy)

        assertEquals(current, result)
    }

    @Test
    fun `newer observation updates identity and presence together`() {
        val current = Player.fromObservation(observation(version = 12, nickname = "OldName", observedAt = "2026-08-27T00:00:00Z"))
        val currentObservation = observation(version = 13, nickname = "CurrentName", observedAt = "2026-08-27T00:01:00Z")

        val result = current.applyObservation(currentObservation, transitionPolicy)

        assertEquals("CurrentName", result.nickname.value)
        assertEquals(13, result.externalVersion)
        assertEquals("SAO-3", result.presence.realm)
    }

    private fun observation(
        version: Long,
        nickname: String,
        observedAt: String,
    ): PlayerObservation =
        PlayerObservation(
            playerId = playerId,
            nickname = PlayerNickname.of(nickname),
            externalVersion = version,
            externalUpdatedAt = Instant.parse(observedAt),
            presence =
                PlayerPresence(
                    status = PlayerPresenceStatus.ONLINE,
                    realm = "SAO-3",
                    realmType = "SAO",
                    privacyHidden = false,
                    lastSeenOnline = null,
                    observedAt = Instant.parse(observedAt),
                ),
        )
}
