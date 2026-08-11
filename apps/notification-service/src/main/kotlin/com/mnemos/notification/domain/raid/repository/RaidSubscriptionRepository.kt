package com.mnemos.notification.domain.raid.repository

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.domain.raid.model.RaidSubscription
import com.mnemos.notification.domain.subscription.model.SubscriptionId

interface RaidSubscriptionRepository {
    fun createOrGetId(subscription: RaidSubscription): SubscriptionId

    fun findAllLocationsSubscriberUserIds(): Set<UserId>

    fun findSubscriberUserIdsByLocations(locations: Set<Int>): Set<UserId>
}
