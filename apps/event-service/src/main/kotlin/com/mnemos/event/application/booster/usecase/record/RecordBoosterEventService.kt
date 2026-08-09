package com.mnemos.event.application.booster.usecase.record

import com.mnemos.event.application.booster.mapper.BoosterIntegrationEventMapper.toIntegrationEvent
import com.mnemos.event.domain.booster.model.BoosterEvent
import com.mnemos.event.domain.booster.repository.BoosterEventRepository
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import org.springframework.stereotype.Service

@Service
class RecordBoosterEventService(
    private val boosterEventRepository: BoosterEventRepository,
    private val publisher: IntegrationEventPublisher,
) : RecordBoosterEventUseCase {
    override fun execute(command: RecordBoosterEventCommand): RecordBoosterEventResult {
        val event =
            BoosterEvent(
                type = command.type,
                activatedBy = command.activatedBy,
                activatedAt = command.activatedAt,
            )
        val saved = boosterEventRepository.save(event)
        publisher.publish(saved.toIntegrationEvent())
        return RecordBoosterEventResult(saved.id)
    }
}
