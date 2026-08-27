package com.mnemos.event.application.booster.usecase.record

import com.mnemos.event.application.booster.mapper.BoosterIntegrationEventMapper.toIntegrationEvent
import com.mnemos.event.application.playeridentity.port.out.PlayerIdentityProjectionRepository
import com.mnemos.event.domain.booster.model.BoosterEvent
import com.mnemos.event.domain.booster.repository.BoosterEventRepository
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

@Service
@Transactional
class RecordBoosterEventService(
    private val boosterEventRepository: BoosterEventRepository,
    private val playerIdentityRepository: PlayerIdentityProjectionRepository,
    private val publisher: IntegrationEventPublisher,
    private val clock: Clock,
) : RecordBoosterEventUseCase {
    override fun execute(command: RecordBoosterEventCommand): RecordBoosterEventResult {
        val playerId = playerIdentityRepository.findCurrentOwnerId(command.activatedBy)
        val resolutionRequestedAt = if (playerId == null) Instant.now(clock) else null
        val event =
            BoosterEvent(
                type = command.type,
                playerId = playerId,
                activatedBy = command.activatedBy,
                activatedAt = command.activatedAt,
                identityResolutionPending = playerId == null,
                identityResolutionRequestedAt = resolutionRequestedAt,
            )
        val saved = boosterEventRepository.save(event)

        publisher.publish(saved.toIntegrationEvent())

        return RecordBoosterEventResult(saved.id)
    }
}
