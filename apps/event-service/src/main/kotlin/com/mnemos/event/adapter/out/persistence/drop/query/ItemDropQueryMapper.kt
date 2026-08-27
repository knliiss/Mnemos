package com.mnemos.event.adapter.out.persistence.drop.query

import com.mnemos.event.application.itemdrop.model.ItemDropView
import com.mnemos.event.domain.EventId
import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.DomainItemType
import com.mnemos.event.domain.item.model.ItemId
import com.mnemos.event.domain.player.model.CristalixPlayerId

object ItemDropQueryMapper {
    fun ItemDropViewProjection.toView(): ItemDropView =
        ItemDropView(
            id = EventId(id),
            itemId = ItemId(itemId),
            itemName = itemName,
            itemRarity = DomainItemRarity.valueOf(itemRarity),
            itemType = DomainItemType.valueOf(itemType),
            playerId = playerId?.let(::CristalixPlayerId),
            droppedFor = droppedFor,
            occurredAt = occurredAt,
        )
}
