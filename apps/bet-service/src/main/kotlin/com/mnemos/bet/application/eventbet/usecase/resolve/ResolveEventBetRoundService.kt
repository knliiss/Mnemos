package com.mnemos.bet.application.eventbet.usecase.resolve

import com.mnemos.bet.application.eventbet.service.EventBetResolutionProcessor
import com.mnemos.bet.application.security.port.out.CurrentActorProvider
import com.mnemos.bet.domain.eventbet.model.EventBetResolution
import com.mnemos.bet.domain.eventbet.repository.EventBetRepository
import com.mnemos.bet.domain.exception.BetConflictException
import com.mnemos.bet.domain.exception.BetNotFoundException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock

@Service
class ResolveEventBetRoundService(
    private val repository: EventBetRepository,
    private val currentActorProvider: CurrentActorProvider,
    private val resolutionProcessor: EventBetResolutionProcessor,
    private val clock: Clock,
) : ResolveEventBetRoundUseCase {
    @Transactional
    override fun execute(command: ResolveEventBetRoundCommand) {
        require(command.reason.isNotBlank())

        val round =
            repository.findRoundByIdForUpdate(command.roundId)
                ?: throw BetNotFoundException("Event bet round ${command.roundId} was not found")

        if (round.effectiveOutcome == command.outcome) {
            throw BetConflictException("Round already has outcome ${command.outcome}")
        }

        val resolution =
            EventBetResolution.administrative(
                roundId = round.id,
                outcome = command.outcome,
                resolvedByUserId = currentActorProvider.requireUserId(),
                reason = command.reason,
                resolvedAt = clock.instant(),
            )

        repository.createResolution(resolution)
        round.resolveAdministratively(command.outcome)
        repository.saveRound(round)

        resolutionProcessor.process(
            round = round,
            resolution = resolution,
            bets = repository.findBetsByRoundForUpdate(round.id),
        )
    }
}
