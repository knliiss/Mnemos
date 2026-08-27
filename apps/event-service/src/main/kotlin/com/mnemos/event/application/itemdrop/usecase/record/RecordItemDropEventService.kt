package com.mnemos.event.application.itemdrop.usecase.record

import com.mnemos.event.application.itemdrop.mapper.ItemDropIntegrationEventMapper.toIntegrationEvent
import com.mnemos.event.application.playeridentity.port.out.PlayerIdentityProjectionRepository
import com.mnemos.event.domain.drop.model.ItemDropEvent
import com.mnemos.event.domain.drop.repository.ItemDropEventRepository
import com.mnemos.event.domain.item.repository.ItemRepository
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Clock
import java.time.Instant

@Service
@Transactional
class RecordItemDropEventService(
    private val itemRepository: ItemRepository,
    private val itemDropEventRepository: ItemDropEventRepository,
    private val playerIdentityRepository: PlayerIdentityProjectionRepository,
    private val publisher: IntegrationEventPublisher,
    private val clock: Clock,
) : RecordItemDropEventUseCase {
    override fun execute(command: RecordItemDropEventCommand): RecordItemDropResult {
        val item =
            itemRepository.getOrCreate(
                command.itemName,
                command.itemType,
                command.itemRarity,
            )
        val playerId = playerIdentityRepository.findCurrentOwnerId(command.droppedFor)
        val resolutionRequestedAt = if (playerId == null) Instant.now(clock) else null

        val event =
            ItemDropEvent(
                itemId = item.id,
                playerId = playerId,
                droppedFor = command.droppedFor,
                occurredAt = command.occurredAt,
                identityResolutionPending = playerId == null,
                identityResolutionRequestedAt = resolutionRequestedAt,
            )

        val savedEvent = itemDropEventRepository.save(event)
        publisher.publish(savedEvent.toIntegrationEvent(item))

        return RecordItemDropResult(savedEvent.id)
    }
}
