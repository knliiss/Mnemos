package com.mnemos.event.application.raid.usecase.record

import com.mnemos.event.application.raid.mapper.RaidIntegrationEventMapper.toIntegrationEvent
import com.mnemos.event.domain.raid.model.RaidEvent
import com.mnemos.event.domain.raid.repository.RaidEventRepository
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class RecordRaidEventService(
    private val raidEventRepository: RaidEventRepository,
    private val publisher: IntegrationEventPublisher,
) : RecordRaidEventUseCase {
    override fun execute(command: RecordRaidEventCommand): RecordRaidEventResult {
        val raidEvent =
            RaidEvent(
                locations = command.locations,
                startedAt = command.startedAt,
            )
        val saved = raidEventRepository.save(raidEvent)
        publisher.publish(saved.toIntegrationEvent())
        return RecordRaidEventResult(saved.id)
    }
}
