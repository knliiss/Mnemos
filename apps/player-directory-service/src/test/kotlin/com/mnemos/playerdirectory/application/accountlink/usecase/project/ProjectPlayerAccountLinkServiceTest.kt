package com.mnemos.playerdirectory.application.accountlink.usecase.project

import com.mnemos.playerdirectory.application.accountlink.port.out.PlayerAccountLinkRepository
import com.mnemos.playerdirectory.application.player.port.out.PlayerRepository
import com.mnemos.playerdirectory.application.player.port.out.PlayerIntegrationEventPublisher
import com.mnemos.playerdirectory.application.tracking.port.out.PlayerTrackingRepository
import com.mnemos.playerdirectory.domain.tracking.model.PlayerTrackingState
import com.mnemos.playerdirectory.domain.player.model.CristalixPlayerId
import com.mnemos.playerdirectory.domain.player.model.Player
import com.mnemos.playerdirectory.domain.player.model.PlayerNickname
import com.mnemos.playerdirectory.domain.player.model.PlayerObservation
import com.mnemos.playerdirectory.domain.player.model.PlayerPresence
import com.mnemos.playerdirectory.domain.player.model.PlayerPresenceStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class ProjectPlayerAccountLinkServiceTest {
    @Test
    fun `linking an account seeds a durable identity poll for its immutable player id`() {
        val linkedRepository = mock<PlayerAccountLinkRepository>()
        val trackingRepository = mock<PlayerTrackingRepository>()
        val playerRepository = mock<PlayerRepository>()
        val eventPublisher = mock<PlayerIntegrationEventPublisher>()
        val now = Instant.parse("2026-10-16T00:00:00Z")
        val service =
            ProjectPlayerAccountLinkService(
                repository = linkedRepository,
                trackingRepository = trackingRepository,
                playerRepository = playerRepository,
                eventPublisher = eventPublisher,
                clock = Clock.fixed(now, ZoneOffset.UTC),
            )
        val playerId = UUID.fromString("11111111-1111-1111-1111-111111111111")
        val accountId = UUID.fromString("22222222-2222-2222-2222-222222222222")
        val command =
            ProjectPlayerAccountLinkCommand(
                playerId = playerId,
                accountId = accountId,
                recordedAt = now.minusSeconds(86400),
            )

        service.execute(command)

        val link = argumentCaptor<com.mnemos.playerdirectory.application.accountlink.model.PlayerAccountLink>()
        val tracking = argumentCaptor<PlayerTrackingState>()
        verify(linkedRepository).save(link.capture())
        verify(trackingRepository).ensureState(tracking.capture())
        assertEquals(playerId, link.firstValue.playerId.id)
        assertEquals(accountId, link.firstValue.accountId)
        assertEquals(playerId, tracking.firstValue.playerId.id)
        assertEquals(now, tracking.firstValue.nextPollAt)
    }
}    @Test
    fun `account bootstrap republishes an existing player identity to heal old nickname snapshots`() {
        val links = mock<PlayerAccountLinkRepository>()
        val tracking = mock<PlayerTrackingRepository>()
        val players = mock<PlayerRepository>()
        val events = mock<PlayerIntegrationEventPublisher>()
        val now = Instant.parse("2026-10-16T00:00:00Z")
        val id = CristalixPlayerId(UUID.fromString("11111111-1111-1111-1111-111111111111"))
        val observation =
            PlayerObservation(
                playerId = id,
                nickname = PlayerNickname.of("CurrentName"),
                externalVersion = 14,
                externalUpdatedAt = now,
                presence = PlayerPresence(
                    status = PlayerPresenceStatus.OFFLINE,
                    realm = null,
                    realmType = null,
                    privacyHidden = false,
                    lastSeenOnline = null,
                    observedAt = now,
                ),
            )
        val player = Player.fromObservation(observation)
        whenever(players.findById(id)).thenReturn(player)

        val service = ProjectPlayerAccountLinkService(links, tracking, players, events, Clock.fixed(now, ZoneOffset.UTC))
        service.execute(ProjectPlayerAccountLinkCommand(id.id, UUID.fromString("22222222-2222-2222-2222-222222222222"), now))

        verify(events).publishInitial(player, now)
    }


