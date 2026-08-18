package com.mnemos.event.adapter.inbound.http.internal

import com.mnemos.event.application.itemdrop.model.ItemDropView

object InternalItemDropApiMapper {
    fun ItemDropView.toInternalResponse(): InternalItemDropResponse =
        InternalItemDropResponse(
            id = id.id,
            itemId = itemId.id,
            itemName = itemName,
            itemRarity = itemRarity,
            itemType = itemType,
            droppedFor = droppedFor,
            occurredAt = occurredAt,
        )
}
