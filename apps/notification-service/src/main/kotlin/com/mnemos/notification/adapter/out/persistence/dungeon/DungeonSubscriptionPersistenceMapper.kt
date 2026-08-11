package com.mnemos.notification.adapter.out.persistence.dungeon

import com.mnemos.notification.domain.dungeon.model.DungeonSubscription

object DungeonSubscriptionPersistenceMapper {
    fun DungeonSubscription.toEntity(): DungeonSubscriptionEntity =
        DungeonSubscriptionEntity(
            subscriptionId = id.id,
            dungeonType = dungeonType,
        )

    fun DungeonSubscription.toEquivalenceKey(): String = dungeonType.name
}
