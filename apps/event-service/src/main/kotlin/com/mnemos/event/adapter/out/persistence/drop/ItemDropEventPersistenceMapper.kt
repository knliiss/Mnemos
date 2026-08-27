package com.mnemos.event.adapter.out.persistence.drop

import com.mnemos.event.domain.EventId
import com.mnemos.event.domain.drop.model.ItemDropEvent
import com.mnemos.event.domain.item.model.ItemId
import com.mnemos.event.domain.player.model.CristalixPlayerId

object ItemDropEventPersistenceMapper {
    fun ItemDropEvent.toEntity(): ItemDropEventEntity =
        ItemDropEventEntity(
            id = id.id,
            itemId = itemId.id,
            playerId = playerId?.id,
            droppedFor = droppedFor,
            occurredAt = occurredAt,
            identityResolutionPending = identityResolutionPending,
            identityResolutionRequestedAt = identityResolutionRequestedAt,
        )

    fun ItemDropEventEntity.toDomain(): ItemDropEvent =
        ItemDropEvent(
            id = EventId(id),
            itemId = ItemId(itemId),
            playerId = playerId?.let(::CristalixPlayerId),
            droppedFor = droppedFor,
            occurredAt = occurredAt,
            identityResolutionPending = identityResolutionPending,
            identityResolutionRequestedAt = identityResolutionRequestedAt,
        )
}
