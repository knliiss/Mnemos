package com.mnemos.notification.adapter.out.persistence.subscription.query

import com.mnemos.notification.application.subscription.model.SubscriptionView
import com.mnemos.notification.domain.booster.model.DomainBoosterType
import com.mnemos.notification.domain.dungeon.model.DomainDungeonType
import com.mnemos.notification.domain.global.model.DomainGlobalEventType
import com.mnemos.notification.domain.subscription.model.SubscriptionId
import com.mnemos.notification.domain.subscription.model.SubscriptionType

object SubscriptionQueryMapper {
    fun SubscriptionViewProjection.toView(): SubscriptionView {
        val subscriptionType = SubscriptionType.valueOf(type)

        return SubscriptionView(
            id = SubscriptionId(id),
            type = subscriptionType,
            createdAt = createdAt,
            details =
                when (subscriptionType) {
                    SubscriptionType.ITEM_DROP ->
                        SubscriptionView.Details.ItemDrop(
                            droppedFor = droppedFor,
                            itemName = itemName,
                        )

                    SubscriptionType.BOOSTER ->
                        SubscriptionView.Details.Booster(
                            boosterType = DomainBoosterType.valueOf(requireNotNull(boosterType)),
                        )

                    SubscriptionType.DUNGEON ->
                        SubscriptionView.Details.Dungeon(
                            dungeonType = DomainDungeonType.valueOf(requireNotNull(dungeonType)),
                        )

                    SubscriptionType.RAID ->
                        SubscriptionView.Details.Raid(
                            locations =
                                raidLocations
                                    ?.takeIf { it.isNotBlank() }
                                    ?.split(',')
                                    ?.mapTo(linkedSetOf()) { it.toInt() }
                                    ?: emptySet(),
                        )

                    SubscriptionType.GLOBAL ->
                        SubscriptionView.Details.Global(
                            eventType = DomainGlobalEventType.valueOf(requireNotNull(globalEventType)),
                        )
                },
        )
    }
}
