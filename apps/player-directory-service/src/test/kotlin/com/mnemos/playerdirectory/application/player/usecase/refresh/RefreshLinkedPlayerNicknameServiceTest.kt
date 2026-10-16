package com.mnemos.playerdirectory.application.player.usecase.refresh

import com.mnemos.playerdirectory.application.accountlink.port.out.PlayerAccountLinkRepository
import com.mnemos.playerdirectory.application.player.exception.PlayerNotFoundException
import com.mnemos.playerdirectory.application.player.port.out.CristalixPlayerGateway
import com.mnemos.playerdirectory.application.player.port.out.PlayerIntegrationEventPublisher
import com.mnemos.playerdirectory.application.player.port.out.PlayerRepository
import com.mnemos.playerdirectory.application.player.support.PlayerObservationCoordinator
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.player.model.Player
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import com.mnemos.playerdirectory.domain.player.model.PlayerObservation
import com.mnemos.playerdirectory.domain.player.model.PlayerPresence
import com.mnemos.playerdirectory.domain.player.model.PlayerPresenceStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class RefreshLinkedPlayerNicknameServiceTest {
    private val links = mock<PlayerAccountLinkRepository>()
    private val upstream = mock<CristalixPlayerGateway>()
    private val players = mock<PlayerRepository>()
    private val coordinator = mock<PlayerObservationCoordinator>()
    private val events = mock<PlayerIntegrationEventPublisher>()
    private val service = RefreshLinkedPlayerNicknameService(links, upstream, players, coordinator, events)
    private val playerId = CristalixPlayerId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
    private val observedAt = Instant.parse("2026-10-16T12:00:00Z")

    @Test
    fun `admin refresh checks immutable player id and republishes latest identity`() {
        val previous = player("OldName")
        val current = player("Renamed")
        val observation = observation("Renamed")
        whenever(links.existsByPlayerId(playerId)).thenReturn(true)
        whenever(players.findById(playerId)).thenReturn(previous)
        whenever(upstream.findById(playerId)).thenReturn(observation)
        whenever(coordinator.apply(observation)).thenReturn(current)

        val result = service.execute(playerId)

        assertEquals("OldName", result.previousKnownNickname)
        assertEquals("Renamed", result.currentNickname)
        assertTrue(result.nicknameChangedInDirectory)
        assertEquals(playerId.id, result.playerId)
        verify(upstream).findById(playerId)
        verify(coordinator).apply(observation)
        verify(events).publishInitial(current, observedAt)
    }

    @Test
    fun `known current directory nickname is republished to repair a stale account snapshot`() {
        val observation = observation("CurrentName")
        val current = player("CurrentName")
        whenever(links.existsByPlayerId(playerId)).thenReturn(true)
        whenever(players.findById(playerId)).thenReturn(current)
        whenever(upstream.findById(playerId)).thenReturn(observation)
        whenever(coordinator.apply(observation)).thenReturn(current)

        val result = service.execute(playerId)

        assertFalse(result.nicknameChangedInDirectory)
        verify(events).publishInitial(current, observedAt)
    }

    @Test
    fun `unlinked UUID cannot consume manual Cristalix API requests`() {
        assertThrows(PlayerNotFoundException::class.java) {
            service.execute(playerId)
        }
        verify(upstream, never()).findById(playerId)
    }

    private fun player(name: String): Player = Player.fromObservation(observation(name))

    private fun observation(name: String): PlayerObservation =
        PlayerObservation(
            playerId = playerId,
            nickname = PlayerNickname.of(name),
            externalVersion = 14L,
            externalUpdatedAt = observedAt,
            presence =
                PlayerPresence(
                    status = PlayerPresenceStatus.OFFLINE,
                    realm = null,
                    realmType = null,
                    privacyHidden = false,
                    lastSeenOnline = null,
                    observedAt = observedAt,
                ),
        )
}
