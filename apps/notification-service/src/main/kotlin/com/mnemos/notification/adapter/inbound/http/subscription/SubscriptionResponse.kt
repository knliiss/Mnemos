package com.mnemos.notification.adapter.inbound.http.subscription

import com.mnemos.notification.domain.booster.model.DomainBoosterType
import com.mnemos.notification.domain.dungeon.model.DomainDungeonType
import com.mnemos.notification.domain.global.model.DomainGlobalEventType
import com.mnemos.notification.domain.subscription.model.SubscriptionType
import java.time.Instant
import java.util.UUID

data class SubscriptionResponse(
    val id: UUID,
    val type: SubscriptionType,
    val createdAt: Instant,
    val details: Details,
) {
    sealed interface Details {
        data class ItemDrop(
            val droppedFor: String?,
            val itemName: String?,
        ) : Details

        data class Booster(
            val boosterType: DomainBoosterType,
        ) : Details

        data class Dungeon(
            val dungeonType: DomainDungeonType,
        ) : Details

        data class Raid(
            val locations: Set<Int>,
        ) : Details

        data class Global(
            val eventType: DomainGlobalEventType,
        ) : Details
    }
}
