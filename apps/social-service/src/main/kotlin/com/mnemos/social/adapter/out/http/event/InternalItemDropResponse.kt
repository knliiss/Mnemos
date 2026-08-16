package com.mnemos.social.adapter.out.http.event

import java.time.Instant
import java.util.UUID

data class InternalItemDropResponse(
    val id: UUID,
    val itemId: UUID,
    val itemName: String,
    val itemRarity: String,
    val itemType: String,
    val droppedFor: String,
    val occurredAt: Instant,
)
