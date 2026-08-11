package com.mnemos.notification.adapter.out.persistence.booster

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.adapter.out.persistence.booster.BoosterSubscriptionPersistenceMapper.toEntity
import com.mnemos.notification.adapter.out.persistence.booster.BoosterSubscriptionPersistenceMapper.toEquivalenceKey
import com.mnemos.notification.adapter.out.persistence.subscription.SubscriptionPersistenceWriter
import com.mnemos.notification.domain.booster.model.BoosterSubscription
import com.mnemos.notification.domain.booster.model.DomainBoosterType
import com.mnemos.notification.domain.booster.repository.BoosterSubscriptionRepository
import com.mnemos.notification.domain.subscription.model.SubscriptionId
import org.springframework.stereotype.Repository

@Repository
class BoosterSubscriptionRepositoryAdapter(
    private val jpaRepository: BoosterSubscriptionJpaRepository,
    private val subscriptionPersistenceWriter: SubscriptionPersistenceWriter,
) : BoosterSubscriptionRepository {
    override fun createOrGetId(subscription: BoosterSubscription): SubscriptionId =
        subscriptionPersistenceWriter.createOrGetId(
            subscription = subscription,
            equivalenceKey = subscription.toEquivalenceKey(),
        ) {
            jpaRepository.save(subscription.toEntity())
        }

    override fun findMatchingUserIds(boosterType: DomainBoosterType): Set<UserId> =
        jpaRepository
            .findMatchingUserIds(boosterType)
            .mapTo(HashSet(), ::UserId)
}
