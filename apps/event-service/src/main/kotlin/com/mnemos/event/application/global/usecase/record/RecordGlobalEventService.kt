package com.mnemos.event.application.global.usecase.record

import com.mnemos.event.application.global.mapper.GlobalIntegrationEventMapper.toIntegrationEvent
import com.mnemos.event.domain.global.model.GlobalEvent
import com.mnemos.event.domain.global.repository.GlobalEventRepository
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class RecordGlobalEventService(
    private val globalEventRepository: GlobalEventRepository,
    private val publisher: IntegrationEventPublisher,
) : RecordGlobalEventUseCase {
    override fun execute(command: RecordGlobalEventCommand): RecordGlobalEventResult {
        val event =
            GlobalEvent(
                type = command.type,
                startedAt = command.startedAt,
            )
        val saved = globalEventRepository.save(event)
        publisher.publish(saved.toIntegrationEvent())
        return RecordGlobalEventResult(saved.id)
    }
}
