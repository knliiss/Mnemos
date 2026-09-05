package com.mnemos.realtime.application.broadcast.model

import java.time.Instant
import java.util.UUID

data class GameEventBroadcastPayload(
    val eventId: UUID,
    val eventType: String,
    val occurredAt: Instant,
    val itemDrop: ItemDrop?,
    val raid: Raid?,
    val booster: Booster?,
    val global: Global?,
) {
    data class ItemDrop(
        val itemId: UUID,
        val itemKey: String? = null,
        val itemName: String,
        val itemType: String,
        val itemRarity: String,
        val droppedFor: String,
    )

    data class Raid(
        val locations: Set<Int>,
        val locationLocalizationKeys: List<String>,
    )

    data class Booster(
        val activatedBy: String,
        val type: String,
        val localizationKey: String,
    )

    data class Global(
        val type: String,
        val localizationKey: String,
    )

    init {
        val populatedPayloads = listOfNotNull(itemDrop, raid, booster, global)
        require(populatedPayloads.size == 1) {
            "Game event broadcast must contain exactly one typed payload"
        }
    }
}
