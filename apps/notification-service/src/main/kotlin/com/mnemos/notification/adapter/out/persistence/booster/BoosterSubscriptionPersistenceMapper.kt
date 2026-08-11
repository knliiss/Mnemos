package com.mnemos.notification.adapter.out.persistence.booster

import com.mnemos.notification.domain.booster.model.BoosterSubscription

object BoosterSubscriptionPersistenceMapper {
    fun BoosterSubscription.toEntity(): BoosterSubscriptionEntity =
        BoosterSubscriptionEntity(
            subscriptionId = id.id,
            boosterType = boosterType,
        )

    fun BoosterSubscription.toEquivalenceKey(): String = boosterType.name
}
