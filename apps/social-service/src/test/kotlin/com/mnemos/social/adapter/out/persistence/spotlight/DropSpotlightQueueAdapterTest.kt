package com.mnemos.social.adapter.out.persistence.spotlight

import com.mnemos.social.configuration.spotlight.DropSpotlightProperties
import com.mnemos.social.domain.spotlight.model.DropSpotlightStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Duration
import java.time.Instant

class DropSpotlightQueueAdapterTest {
    @Test
    fun `schedules after latest non failed spotlight and increments sequence`() {
        val now = Instant.parse("2026-08-16T00:00:00Z")
        val latestScheduledAt = now.plusSeconds(10)
        val state =
            SpotlightQueueStateEntity(
                id = 1,
                nextSequence = 7,
            )
        val latest = mock<DropSpotlightEntity>()
        val queueStateRepository = mock<SpotlightQueueStateJpaRepository>()
        val spotlightRepository = mock<DropSpotlightJpaRepository>()
        val adapter =
            DropSpotlightQueueAdapter(
                queueStateRepository = queueStateRepository,
                spotlightRepository = spotlightRepository,
                properties = properties(),
            )

        whenever(queueStateRepository.findForUpdate()).thenReturn(state)
        whenever(latest.scheduledAt).thenReturn(latestScheduledAt)
        whenever(spotlightRepository.findTopByStatusInOrderByScheduledAtDesc(any()))
            .thenReturn(latest)

        val reservation = adapter.reserve(now)

        assertEquals(7, reservation.sequence)
        assertEquals(latestScheduledAt.plusSeconds(15), reservation.scheduledAt)
        assertEquals(8, state.nextSequence)
        verify(queueStateRepository).save(state)

        val statuses = argumentCaptor<Collection<DropSpotlightStatus>>()
        verify(spotlightRepository).findTopByStatusInOrderByScheduledAtDesc(statuses.capture())

        assertFalse(statuses.firstValue.contains(DropSpotlightStatus.PAYMENT_FAILED))
    }

    @Test
    fun `schedules immediately when no non failed spotlight exists`() {
        val now = Instant.parse("2026-08-16T00:00:00Z")
        val state =
            SpotlightQueueStateEntity(
                id = 1,
                nextSequence = 3,
            )
        val queueStateRepository = mock<SpotlightQueueStateJpaRepository>()
        val spotlightRepository = mock<DropSpotlightJpaRepository>()
        val adapter =
            DropSpotlightQueueAdapter(
                queueStateRepository = queueStateRepository,
                spotlightRepository = spotlightRepository,
                properties = properties(),
            )

        whenever(queueStateRepository.findForUpdate()).thenReturn(state)
        whenever(spotlightRepository.findTopByStatusInOrderByScheduledAtDesc(any()))
            .thenReturn(null)

        val reservation = adapter.reserve(now)

        assertEquals(now, reservation.scheduledAt)
        assertEquals(3, reservation.sequence)
    }

    private fun properties(): DropSpotlightProperties =
        DropSpotlightProperties(
            pricePrime = 25,
            dropMaxAge = Duration.ofHours(2),
            lifetime = Duration.ofHours(1),
            buyerCooldown = Duration.ofMinutes(20),
            globalGap = Duration.ofSeconds(15),
            recipientGap = Duration.ofSeconds(30),
            maxQueueDelay = Duration.ofMinutes(5),
        )
}
