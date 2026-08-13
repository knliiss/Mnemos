package com.mnemos.bet.application.itemrace.usecase.create

import com.mnemos.bet.configuration.ItemRaceProperties
import com.mnemos.bet.contracts.BetEventType
import com.mnemos.bet.contracts.event.ItemRaceOpenedV1
import com.mnemos.bet.contracts.shared.BetItemType
import com.mnemos.bet.domain.itemrace.model.ItemRace
import com.mnemos.bet.domain.itemrace.repository.ItemRaceRepository
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Duration

@Service
class CreateItemRaceService(
    private val repository: ItemRaceRepository,
    private val properties: ItemRaceProperties,
    private val publisher: IntegrationEventPublisher,
    private val clock: Clock,
) : CreateItemRaceUseCase {
    @Transactional
    override fun execute(command: CreateItemRaceCommand): CreateItemRaceResult {
        require(command.itemName.isNotBlank())
        require(command.durationHours in MINIMUM_DURATION_HOURS..MAXIMUM_DURATION_HOURS)
        require(command.entryAmount in MINIMUM_ENTRY..MAXIMUM_ENTRY)

        val now = clock.instant()
        val registrationClosesAt = now.plus(properties.registrationDuration)
        val startsAt = registrationClosesAt.plus(properties.entryConfirmationGrace)
        val expiresAt = startsAt.plus(Duration.ofHours(command.durationHours))

        val race =
            ItemRace.create(
                itemName = command.itemName,
                itemType = command.itemType,
                registrationClosesAt = registrationClosesAt,
                startsAt = startsAt,
                expiresAt = expiresAt,
                entryAmount = command.entryAmount,
            )

        repository.create(race)

        publisher.publish(
            IntegrationEvent(
                type = BetEventType.ITEM_RACE_OPENED,
                aggregateId = race.id.id,
                occurredAt = now,
                payload =
                    ItemRaceOpenedV1(
                        raceId = race.id.id,
                        itemName = race.itemName,
                        itemType = BetItemType.valueOf(race.itemType.name),
                        registrationClosesAt = race.registrationClosesAt,
                        startsAt = race.startsAt,
                        expiresAt = race.expiresAt,
                        entryAmount = race.entryAmount,
                    ),
            ),
        )

        return CreateItemRaceResult(
            raceId = race.id,
        )
    }

    companion object {
        const val MINIMUM_ENTRY = 1_000L
        const val MAXIMUM_ENTRY = 5_000L
        const val MINIMUM_DURATION_HOURS = 1L
        const val MAXIMUM_DURATION_HOURS = 7L * 24L
    }
}
