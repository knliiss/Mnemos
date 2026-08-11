package com.mnemos.notification.application.subscription.model

import com.mnemos.notification.domain.booster.model.DomainBoosterType
import com.mnemos.notification.domain.dungeon.model.DomainDungeonType
import com.mnemos.notification.domain.global.model.DomainGlobalEventType
import com.mnemos.notification.domain.subscription.model.SubscriptionId
import com.mnemos.notification.domain.subscription.model.SubscriptionType
import java.time.Instant

data class SubscriptionView(
    val id: SubscriptionId,
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
