package com.mnemos.event.adapter.inbound.http.itemdrop

import com.mnemos.event.domain.item.model.DomainItemRarity
import java.time.Instant
import java.util.UUID

data class ItemDropResponse(
    val id: UUID,
    val itemId: UUID,
    val itemName: String,
    val itemRarity: DomainItemRarity,
    val itemType: String,
    val droppedFor: String,
    val occurredAt: Instant,
)
