package com.mnemos.bet.application.eventbet.usecase.settle

import com.mnemos.bet.application.event.port.out.CollectorCoverageProvider
import com.mnemos.bet.application.eventbet.service.EventBetResolutionProcessor
import com.mnemos.bet.domain.eventbet.model.EventBetOutcome
import com.mnemos.bet.domain.eventbet.model.EventBetResolution
import com.mnemos.bet.domain.eventbet.model.EventBetRound
import com.mnemos.bet.domain.eventbet.model.EventBetRoundStatus
import com.mnemos.bet.domain.eventbet.repository.EventBetRepository
import com.mnemos.bet.domain.occurrence.repository.GlobalOccurrenceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Duration
import java.time.Instant

@Service
class SynchronizeEventBetRoundsService(
    private val repository: EventBetRepository,
    private val occurrenceRepository: GlobalOccurrenceRepository,
    private val collectorCoverageProvider: CollectorCoverageProvider,
    private val resolutionProcessor: EventBetResolutionProcessor,
    private val clock: Clock,
) : SynchronizeEventBetRoundsUseCase {
    @Transactional
    override fun execute() {
        val now = clock.instant()

        repository
            .findRoundsRequiringSynchronization(now)
            .forEach { round -> synchronize(round, now) }
    }

    private fun synchronize(
        round: EventBetRound,
        now: Instant,
    ) {
        round.synchronize(now)
        repository.saveRound(round)

        if (round.status == EventBetRoundStatus.SETTLED || now.isBefore(round.resolvesAt)) {
            return
        }

        val windowFrom = round.scheduledAt.minus(EVENT_TIME_TOLERANCE)
        val occurrence =
            occurrenceRepository.findFirst(
                eventType = round.eventType,
                from = windowFrom,
                to = round.resolvesAt,
            )

        if (occurrence != null) {
            resolveAutomatically(round, EventBetOutcome.YES, now)
            return
        }

        val coverage =
            collectorCoverageProvider.getCoverage(
                from = round.scheduledAt,
                to = round.scheduledAt,
            )

        if (coverage.covered) {
            resolveAutomatically(round, EventBetOutcome.NO, now)
        } else if (round.status != EventBetRoundStatus.AWAITING_ADMIN_CONFIRMATION) {
            round.awaitAdministrativeConfirmation()
            repository.saveRound(round)
        }
    }

    private fun resolveAutomatically(
        round: EventBetRound,
        outcome: EventBetOutcome,
        now: Instant,
    ) {
        val resolution =
            EventBetResolution.automatic(
                roundId = round.id,
                outcome = outcome,
                resolvedAt = now,
            )

        repository.createResolution(resolution)
        round.resolveAutomatically(outcome)
        repository.saveRound(round)

        resolutionProcessor.process(
            round = round,
            resolution = resolution,
            bets = repository.findBetsByRoundForUpdate(round.id),
        )
    }

    companion object {
        private val EVENT_TIME_TOLERANCE: Duration = Duration.ofSeconds(30)
    }
}
