package com.mnemos.playerdirectory.adapter.out.http.cristalix

import com.mnemos.playerdirectory.adapter.out.http.cristalix.CristalixPlayerApiMapper.toObservation
import com.mnemos.playerdirectory.domain.player.model.PlayerPresenceStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class CristalixPlayerApiMapperTest {
    private val observedAt = Instant.parse("2026-08-27T00:00:00Z")

    @Test
    fun `explicit online status maps to online presence`() {
        val response = response(online = true)

        val observation = response.toObservation(observedAt)

        assertEquals(PlayerPresenceStatus.ONLINE, observation.presence.status)
        assertEquals("SAO-3", observation.presence.realm)
    }

    @Test
    fun `explicit offline status maps to offline presence`() {
        val response = response(online = false)

        val observation = response.toObservation(observedAt)

        assertEquals(PlayerPresenceStatus.OFFLINE, observation.presence.status)
    }

    @Test
    fun `hidden privacy overrides an online status`() {
        val response = response(online = true, privacyHidden = true)

        val observation = response.toObservation(observedAt)

        assertEquals(PlayerPresenceStatus.HIDDEN, observation.presence.status)
    }

    @Test
    fun `disabled online status setting maps to hidden presence`() {
        val response =
            response(
                online = true,
                settings = mapOf("showOnlineStatus" to "false"),
            )

        val observation = response.toObservation(observedAt)

        assertEquals(PlayerPresenceStatus.HIDDEN, observation.presence.status)
    }

    @Test
    fun `missing online field maps to unknown instead of offline`() {
        val response = response(online = null)

        val observation = response.toObservation(observedAt)

        assertEquals(PlayerPresenceStatus.UNKNOWN, observation.presence.status)
    }

    private fun response(
        online: Boolean?,
        privacyHidden: Boolean = false,
        settings: Map<String, String> = emptyMap(),
    ): CristalixPlayerResponse =
        CristalixPlayerResponse(
            playerId = UUID.fromString("ea0a316c-ec21-11ea-acca-1cb72caa35fd"),
            name = "KoTuK_PvP",
            version = 485,
            updatedAt = Instant.parse("2026-04-15T01:20:03.944Z"),
            social = CristalixPlayerResponse.Social(lastSeenOnline = Instant.parse("2026-08-26T23:59:30Z")),
            status =
                CristalixPlayerResponse.Status(
                    online = online,
                    realm = "SAO-3",
                    realmType = "SAO",
                    privacyHidden = privacyHidden,
                ),
            customization = CristalixPlayerResponse.Customization(settings = settings),
        )
}
