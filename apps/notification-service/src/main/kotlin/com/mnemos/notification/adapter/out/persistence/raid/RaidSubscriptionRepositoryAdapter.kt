package com.mnemos.notification.adapter.out.persistence.raid

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.adapter.out.persistence.raid.RaidSubscriptionPersistenceMapper.toEntity
import com.mnemos.notification.adapter.out.persistence.raid.RaidSubscriptionPersistenceMapper.toEquivalenceKey
import com.mnemos.notification.adapter.out.persistence.subscription.SubscriptionPersistenceWriter
import com.mnemos.notification.domain.raid.model.RaidSubscription
import com.mnemos.notification.domain.raid.repository.RaidSubscriptionRepository
import com.mnemos.notification.domain.subscription.model.SubscriptionId
import org.springframework.stereotype.Repository

@Repository
class RaidSubscriptionRepositoryAdapter(
    private val jpaRepository: RaidSubscriptionJpaRepository,
    private val subscriptionPersistenceWriter: SubscriptionPersistenceWriter,
) : RaidSubscriptionRepository {
    override fun createOrGetId(subscription: RaidSubscription): SubscriptionId =
        subscriptionPersistenceWriter.createOrGetId(
            subscription = subscription,
            equivalenceKey = subscription.toEquivalenceKey(),
        ) {
            jpaRepository.save(subscription.toEntity())
        }

    override fun findAllLocationsSubscriberUserIds(): Set<UserId> =
        jpaRepository
            .findAllLocationsSubscriberUserIds()
            .mapTo(HashSet(), ::UserId)

    override fun findSubscriberUserIdsByLocations(locations: Set<Int>): Set<UserId> =
        jpaRepository
            .findSubscriberUserIdsByLocations(locations)
            .mapTo(HashSet(), ::UserId)
}
