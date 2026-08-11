package com.mnemos.notification.domain.dungeon.repository

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.domain.dungeon.model.DomainDungeonType
import com.mnemos.notification.domain.dungeon.model.DungeonSubscription
import com.mnemos.notification.domain.subscription.model.SubscriptionId

interface DungeonSubscriptionRepository {
    fun createOrGetId(subscription: DungeonSubscription): SubscriptionId

    fun findMatchingUserIds(dungeonType: DomainDungeonType): Set<UserId>
}
