package com.mnemos.playerdirectory.application.accountlink.usecase.project

import com.mnemos.playerdirectory.application.accountlink.port.out.PlayerAccountLinkRepository
import com.mnemos.playerdirectory.application.player.port.out.PlayerRepository
import com.mnemos.playerdirectory.application.player.port.out.PlayerIntegrationEventPublisher
import com.mnemos.playerdirectory.application.tracking.port.out.PlayerTrackingRepository
import com.mnemos.playerdirectory.domain.tracking.model.PlayerTrackingState
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
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
}
