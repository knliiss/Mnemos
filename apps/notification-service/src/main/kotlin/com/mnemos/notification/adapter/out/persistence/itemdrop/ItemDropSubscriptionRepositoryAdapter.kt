package com.mnemos.notification.adapter.out.persistence.itemdrop

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.adapter.out.persistence.itemdrop.ItemDropSubscriptionPersistenceMapper.toEntity
import com.mnemos.notification.adapter.out.persistence.itemdrop.ItemDropSubscriptionPersistenceMapper.toEquivalenceKey
import com.mnemos.notification.adapter.out.persistence.subscription.SubscriptionPersistenceWriter
import com.mnemos.notification.domain.itemdrop.model.ItemDropSubscription
import com.mnemos.notification.domain.itemdrop.repository.ItemDropSubscriptionRepository
import com.mnemos.notification.domain.subscription.model.SubscriptionId
import org.springframework.stereotype.Repository

@Repository
class ItemDropSubscriptionRepositoryAdapter(
    private val jpaRepository: ItemDropSubscriptionJpaRepository,
    private val subscriptionPersistenceWriter: SubscriptionPersistenceWriter,
) : ItemDropSubscriptionRepository {
    override fun createOrGetId(subscription: ItemDropSubscription): SubscriptionId =
        subscriptionPersistenceWriter.createOrGetId(
            subscription = subscription,
            equivalenceKey = subscription.toEquivalenceKey(),
        ) {
            jpaRepository.save(subscription.toEntity())
        }

    override fun findMatchingUserIds(
        droppedFor: String,
        itemName: String,
    ): Set<UserId> =
        jpaRepository
            .findMatchingUserIds(droppedFor, itemName)
            .mapTo(HashSet(), ::UserId)
}
