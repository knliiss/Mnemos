package com.mnemos.event.adapter.out.persistence.drop.query

import java.time.Instant
import java.util.UUID

data class ItemDropViewProjection(
    val id: UUID,
    val itemId: UUID,
    val itemName: String,
    val itemType: String,
    val itemRarity: String,
    val playerId: UUID?,
    val droppedFor: String,
    val occurredAt: Instant,
)
