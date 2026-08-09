package com.mnemos.event.application.itemdrop.usecase.record

import com.mnemos.event.application.itemdrop.mapper.ItemDropIntegrationEventMapper.toIntegrationEvent
import com.mnemos.event.domain.drop.model.ItemDropEvent
import com.mnemos.event.domain.drop.repository.ItemDropEventRepository
import com.mnemos.event.domain.item.repository.ItemRepository
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional
class RecordItemDropEventService(
    private val itemRepository: ItemRepository,
    private val itemDropEventRepository: ItemDropEventRepository,
    private val publisher: IntegrationEventPublisher,
) : RecordItemDropEventUseCase {
    override fun execute(command: RecordItemDropEventCommand): RecordItemDropResult {
        val item =
            itemRepository.getOrCreate(
                command.itemName,
                command.itemType,
                command.itemRarity,
            )

        val event =
            ItemDropEvent(
                itemId = item.id,
                droppedFor = command.droppedFor,
                occurredAt = command.occurredAt,
            )

        val savedEvent = itemDropEventRepository.save(event)
        publisher.publish(savedEvent.toIntegrationEvent(item))
        return RecordItemDropResult(savedEvent.id)
    }
}
