package com.mnemos.event.adapter.inbound.http.internal

import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.DomainItemType
import java.time.Instant
import java.util.UUID

data class InternalItemDropResponse(
    val id: UUID,
    val itemId: UUID,
    val itemName: String,
    val itemRarity: DomainItemRarity,
    val itemType: DomainItemType,
    val droppedFor: String,
    val occurredAt: Instant,
)
