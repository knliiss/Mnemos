package com.mnemos.notification.adapter.out.persistence.global

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.adapter.out.persistence.global.GlobalSubscriptionPersistenceMapper.toEntity
import com.mnemos.notification.adapter.out.persistence.global.GlobalSubscriptionPersistenceMapper.toEquivalenceKey
import com.mnemos.notification.adapter.out.persistence.subscription.SubscriptionPersistenceWriter
import com.mnemos.notification.domain.global.model.DomainGlobalEventType
import com.mnemos.notification.domain.global.model.GlobalSubscription
import com.mnemos.notification.domain.global.repository.GlobalSubscriptionRepository
import com.mnemos.notification.domain.subscription.model.SubscriptionId
import org.springframework.stereotype.Repository

@Repository
class GlobalSubscriptionRepositoryAdapter(
    private val jpaRepository: GlobalSubscriptionJpaRepository,
    private val subscriptionPersistenceWriter: SubscriptionPersistenceWriter,
) : GlobalSubscriptionRepository {
    override fun createOrGetId(subscription: GlobalSubscription): SubscriptionId =
        subscriptionPersistenceWriter.createOrGetId(
            subscription = subscription,
            equivalenceKey = subscription.toEquivalenceKey(),
        ) {
            jpaRepository.save(subscription.toEntity())
        }

    override fun findMatchingUserIds(eventType: DomainGlobalEventType): Set<UserId> =
        jpaRepository
            .findMatchingUserIds(eventType)
            .mapTo(HashSet(), ::UserId)
}
