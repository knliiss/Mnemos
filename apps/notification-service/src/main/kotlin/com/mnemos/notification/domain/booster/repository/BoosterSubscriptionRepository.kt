package com.mnemos.notification.domain.booster.repository

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.domain.booster.model.BoosterSubscription
import com.mnemos.notification.domain.booster.model.DomainBoosterType
import com.mnemos.notification.domain.subscription.model.SubscriptionId

interface BoosterSubscriptionRepository {
    fun createOrGetId(subscription: BoosterSubscription): SubscriptionId

    fun findMatchingUserIds(boosterType: DomainBoosterType): Set<UserId>
}
