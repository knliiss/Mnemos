package com.mnemos.event.adapter.out.persistence.drop.query

import com.mnemos.event.application.itemdrop.model.ItemDropView
import com.mnemos.event.domain.EventId
import com.mnemos.event.domain.item.model.ItemId

object ItemDropQueryMapper {
    fun ItemDropViewProjection.toView(): ItemDropView =
        ItemDropView(
            id = EventId(id),
            itemId = ItemId(itemId),
            itemName = itemName,
            itemRarity = itemRarity,
            itemType = itemType,
            droppedFor = droppedFor,
            occurredAt = occurredAt,
        )
}
