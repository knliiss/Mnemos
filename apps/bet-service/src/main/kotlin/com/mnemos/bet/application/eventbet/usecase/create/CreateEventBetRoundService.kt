package com.mnemos.bet.application.eventbet.usecase.create

import com.mnemos.bet.configuration.EventBetProperties
import com.mnemos.bet.contracts.BetEventType
import com.mnemos.bet.contracts.event.EventBetOpenedV1
import com.mnemos.bet.contracts.shared.BetGlobalEventType
import com.mnemos.bet.domain.eventbet.model.EventBetRound
import com.mnemos.bet.domain.eventbet.repository.EventBetRepository
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class CreateEventBetRoundService(
    private val repository: EventBetRepository,
    private val properties: EventBetProperties,
    private val publisher: IntegrationEventPublisher,
) : CreateEventBetRoundUseCase {
    @Transactional
    override fun execute(command: CreateEventBetRoundCommand): CreateEventBetRoundResult {
        val existing =
            repository.findRoundByEventAndScheduleForUpdate(
                eventType = command.eventType,
                scheduledAt = command.scheduledAt,
            )

        if (existing != null) {
            return CreateEventBetRoundResult(
                roundId = existing.id,
                created = false,
            )
        }

        val closesAt = command.scheduledAt.minus(properties.closeBefore)
        val resolvesAt = command.scheduledAt.plus(properties.resultGrace)
        val odds = properties.oddsFor(command.eventType)

        val round =
            EventBetRound.create(
                eventType = command.eventType,
                scheduledAt = command.scheduledAt,
                closesAt = closesAt,
                resolvesAt = resolvesAt,
                yesOdds = odds.yes,
                noOdds = odds.no,
            )

        repository.createRound(round)

        publisher.publish(
            IntegrationEvent(
                type = BetEventType.EVENT_BET_OPENED,
                aggregateId = round.id.id,
                occurredAt = closesAt,
                payload =
                    EventBetOpenedV1(
                        roundId = round.id.id,
                        eventType = BetGlobalEventType.valueOf(round.eventType.name),
                        scheduledAt = round.scheduledAt,
                        closesAt = round.closesAt,
                    ),
            ),
        )

        return CreateEventBetRoundResult(
            roundId = round.id,
            created = true,
        )
    }
}
