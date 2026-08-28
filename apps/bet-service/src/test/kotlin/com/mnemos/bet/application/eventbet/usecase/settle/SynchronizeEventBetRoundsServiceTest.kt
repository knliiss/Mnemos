package com.mnemos.bet.application.eventbet.usecase.settle

import com.mnemos.bet.application.event.model.CollectorCoverage
import com.mnemos.bet.application.event.port.out.CollectorCoverageProvider
import com.mnemos.bet.application.eventbet.service.EventBetResolutionProcessor
import com.mnemos.bet.domain.eventbet.model.DomainBetGlobalEventType
import com.mnemos.bet.domain.eventbet.model.EventBetOutcome
import com.mnemos.bet.domain.eventbet.model.EventBetResolution
import com.mnemos.bet.domain.eventbet.model.EventBetRound
import com.mnemos.bet.domain.eventbet.model.EventBetRoundStatus
import com.mnemos.bet.domain.eventbet.repository.EventBetRepository
import com.mnemos.bet.domain.occurrence.model.ExternalEventId
import com.mnemos.bet.domain.occurrence.model.GlobalOccurrence
import com.mnemos.bet.domain.occurrence.repository.GlobalOccurrenceRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.math.BigDecimal
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class SynchronizeEventBetRoundsServiceTest {
    private val now = Instant.parse("2026-08-14T12:02:00Z")
    private val repository = mock<EventBetRepository>()
    private val occurrenceRepository = mock<GlobalOccurrenceRepository>()
    private val collectorCoverageProvider = mock<CollectorCoverageProvider>()
    private val resolutionProcessor = mock<EventBetResolutionProcessor>()
    private val clock = Clock.fixed(now, ZoneOffset.UTC)

    private val service =
        SynchronizeEventBetRoundsService(
            repository = repository,
            occurrenceRepository = occurrenceRepository,
            collectorCoverageProvider = collectorCoverageProvider,
            resolutionProcessor = resolutionProcessor,
            clock = clock,
        )

    @Test
    fun `missing occurrence becomes no when event window had collector coverage`() {
        val round = createExpiredRound()

        prepareRound(round)
        whenever(collectorCoverageProvider.getCoverage(any(), any())).thenReturn(
            CollectorCoverage(
                covered = true,
                maximumActiveCollectors = 2,
            ),
        )

        service.execute()

        assertEquals(EventBetRoundStatus.SETTLED, round.status)
        assertEquals(EventBetOutcome.NO, round.effectiveOutcome)
        verify(repository).findRoundByIdForUpdate(round.id)
        verify(resolutionProcessor).process(
            round = eq(round),
            resolution = any(),
            bets = eq(emptyList()),
        )
    }

    @Test
    fun `late occurrence resolves round that was waiting for administrator`() {
        val round = createExpiredRound()
        round.synchronize(now)
        round.awaitAdministrativeConfirmation()

        whenever(repository.findRoundsRequiringSynchronization(now)).thenReturn(listOf(round))
        whenever(repository.findRoundByIdForUpdate(round.id)).thenReturn(round)
        whenever(repository.saveRound(any())).thenAnswer { invocation -> invocation.arguments[0] as EventBetRound }
        whenever(repository.createResolution(any())).thenAnswer { invocation -> invocation.arguments[0] as EventBetResolution }
        whenever(repository.findBetsByRoundForUpdate(round.id)).thenReturn(emptyList())
        whenever(
            occurrenceRepository.findFirst(
                eventType = eq(round.eventType),
                from = any(),
                to = any(),
            ),
        ).thenReturn(
            GlobalOccurrence(
                eventId = ExternalEventId(UUID.fromString("0198f6ce-e934-7000-8000-000000000001")),
                eventType = round.eventType,
                occurredAt = round.scheduledAt,
            ),
        )

        service.execute()

        assertEquals(EventBetRoundStatus.SETTLED, round.status)
        assertEquals(EventBetOutcome.YES, round.effectiveOutcome)
        verify(collectorCoverageProvider, never()).getCoverage(any(), any())
        verify(resolutionProcessor).process(
            round = eq(round),
            resolution = any(),
            bets = eq(emptyList()),
        )
    }

    @Test
    fun `missing occurrence waits for administrator without collector coverage`() {
        val round = createExpiredRound()

        prepareRound(round)
        whenever(collectorCoverageProvider.getCoverage(any(), any())).thenReturn(
            CollectorCoverage(
                covered = false,
                maximumActiveCollectors = 0,
            ),
        )

        service.execute()

        assertEquals(EventBetRoundStatus.AWAITING_ADMIN_CONFIRMATION, round.status)
        assertNull(round.effectiveOutcome)
        verify(repository, never()).createResolution(any<EventBetResolution>())
        verify(resolutionProcessor, never()).process(any(), any(), any())
    }

    @Test
    fun `stale scheduler candidate is skipped after another replica settles it`() {
        val staleCandidate = createExpiredRound()
        val lockedRound = createExpiredRound()

        lockedRound.synchronize(now)
        lockedRound.resolveAutomatically(EventBetOutcome.NO)

        whenever(repository.findRoundsRequiringSynchronization(now)).thenReturn(listOf(staleCandidate))
        whenever(repository.findRoundByIdForUpdate(staleCandidate.id)).thenReturn(lockedRound)

        service.execute()

        verify(repository, never()).saveRound(any())
        verify(repository, never()).createResolution(any<EventBetResolution>())
        verify(resolutionProcessor, never()).process(any(), any(), any())
    }

    private fun prepareRound(round: EventBetRound) {
        whenever(repository.findRoundsRequiringSynchronization(now)).thenReturn(listOf(round))
        whenever(repository.findRoundByIdForUpdate(round.id)).thenReturn(round)
        whenever(repository.saveRound(any())).thenAnswer { invocation -> invocation.arguments[0] as EventBetRound }
        whenever(repository.createResolution(any())).thenAnswer { invocation -> invocation.arguments[0] as EventBetResolution }
        whenever(repository.findBetsByRoundForUpdate(round.id)).thenReturn(emptyList())
        whenever(occurrenceRepository.findFirst(any(), any(), any())).thenReturn(null)
    }

    private fun createExpiredRound(): EventBetRound {
        val scheduledAt = Instant.parse("2026-08-14T12:00:00Z")

        return EventBetRound.create(
            eventType = DomainBetGlobalEventType.MOON,
            scheduledAt = scheduledAt,
            closesAt = scheduledAt.minusSeconds(300),
            resolvesAt = scheduledAt.plusSeconds(120),
            yesOdds = BigDecimal("1.9000"),
            noOdds = BigDecimal("1.9000"),
        )
    }
}
