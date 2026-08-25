package com.mnemos.notification.adapter.out.persistence.raid

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.adapter.out.persistence.raid.RaidSubscriptionPersistenceMapper.toEntity
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
    override fun addLocations(subscription: RaidSubscription): SubscriptionId {
        val subscriptionId = createOrGetStableId(subscription)
        val entity = findForUpdate(subscriptionId)
        val mergedLocations = mergeLocations(entity.locations, subscription.locations)

        if (entity.locations != mergedLocations) {
            entity.locations = mergedLocations.toMutableSet()
            jpaRepository.save(entity)
        }

        return subscriptionId
    }

    override fun replaceLocations(subscription: RaidSubscription): SubscriptionId {
        val subscriptionId = createOrGetStableId(subscription)
        val entity = findForUpdate(subscriptionId)
        val replacementLocations = subscription.locations.toMutableSet()

        if (entity.locations != replacementLocations) {
            entity.locations = replacementLocations
            jpaRepository.save(entity)
        }

        return subscriptionId
    }

    override fun findAllLocationsSubscriberUserIds(): Set<UserId> =
        jpaRepository
            .findAllLocationsSubscriberUserIds()
            .mapTo(HashSet(), ::UserId)

    override fun findSubscriberUserIdsByLocations(locations: Set<Int>): Set<UserId> =
        jpaRepository
            .findSubscriberUserIdsByLocations(locations)
            .mapTo(HashSet(), ::UserId)

    private fun createOrGetStableId(subscription: RaidSubscription): SubscriptionId =
        subscriptionPersistenceWriter.createOrGetId(
            subscription = subscription,
            equivalenceKey = RAID_EQUIVALENCE_KEY,
        ) {
            jpaRepository.save(subscription.toEntity())
        }

    private fun findForUpdate(subscriptionId: SubscriptionId): RaidSubscriptionEntity =
        checkNotNull(
            jpaRepository.findBySubscriptionIdForUpdate(subscriptionId.id),
        ) {
            "Raid subscription subtype not found for ${subscriptionId.id}"
        }

    private fun mergeLocations(
        current: Set<Int>,
        incoming: Set<Int>,
    ): Set<Int> {
        if (current.isEmpty() || incoming.isEmpty()) {
            return emptySet()
        }

        return current + incoming
    }

    private companion object {
        const val RAID_EQUIVALENCE_KEY = "raid"
    }
}
