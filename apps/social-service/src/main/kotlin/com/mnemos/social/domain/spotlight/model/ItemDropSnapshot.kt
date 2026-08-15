package com.mnemos.social.domain.spotlight.model

import java.time.Instant
import java.util.UUID

data class ItemDropSnapshot(
    val id: UUID,
    val itemId: UUID,
    val itemName: String,
    val itemRarity: String,
    val itemType: String,
    val droppedFor: String,
    val occurredAt: Instant,
)
