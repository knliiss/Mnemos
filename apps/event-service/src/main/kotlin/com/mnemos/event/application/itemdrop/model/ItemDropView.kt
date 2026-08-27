package com.mnemos.event.application.itemdrop.model

import com.mnemos.event.domain.EventId
import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.DomainItemType
import com.mnemos.event.domain.item.model.ItemId
import com.mnemos.event.domain.player.model.CristalixPlayerId
import java.time.Instant

data class ItemDropView(
    val id: EventId,
    val itemId: ItemId,
    val itemName: String,
    val itemRarity: DomainItemRarity,
    val itemType: DomainItemType,
    val playerId: CristalixPlayerId?,
    val droppedFor: String,
    val occurredAt: Instant,
)
