package com.mnemos.event.adapter.out.persistence.drop.query

import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.DomainItemType
import java.time.Instant
import java.util.UUID

data class ItemDropViewProjection(
    val id: UUID,
    val itemId: UUID,
    val itemName: String,
    val itemType: DomainItemType,
    val itemRarity: DomainItemRarity,
    val droppedFor: String,
    val occurredAt: Instant,
)
