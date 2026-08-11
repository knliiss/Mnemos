package com.mnemos.notification.adapter.out.persistence.raid

import com.mnemos.notification.domain.raid.model.RaidSubscription

object RaidSubscriptionPersistenceMapper {
    fun RaidSubscription.toEntity(): RaidSubscriptionEntity =
        RaidSubscriptionEntity(
            subscriptionId = id.id,
            locations = locations,
        )

    fun RaidSubscription.toEquivalenceKey(): String =
        locations
            .sorted()
            .joinToString(",")
}
