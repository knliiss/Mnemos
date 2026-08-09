package com.mnemos.event.adapter.out.persistence.drop

import com.mnemos.event.domain.EventId
import com.mnemos.event.domain.drop.model.ItemDropEvent
import com.mnemos.event.domain.item.model.ItemId

object ItemDropEventPersistenceMapper {
    fun ItemDropEvent.toEntity(): ItemDropEventEntity =
        ItemDropEventEntity(
            id = id.id,
            itemId = itemId.id,
            droppedFor = droppedFor,
            occurredAt = occurredAt,
        )

    fun ItemDropEventEntity.toDomain(): ItemDropEvent =
        ItemDropEvent(
            id = EventId(id),
            itemId = ItemId(id),
            droppedFor = droppedFor,
            occurredAt = occurredAt,
        )
}
