package com.mnemos.notification.domain.global.repository

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.domain.global.model.DomainGlobalEventType
import com.mnemos.notification.domain.global.model.GlobalSubscription
import com.mnemos.notification.domain.subscription.model.SubscriptionId

interface GlobalSubscriptionRepository {
    fun createOrGetId(subscription: GlobalSubscription): SubscriptionId

    fun findMatchingUserIds(eventType: DomainGlobalEventType): Set<UserId>
}
