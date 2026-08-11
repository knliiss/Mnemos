package com.mnemos.notification.adapter.out.persistence.dungeon

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.adapter.out.persistence.dungeon.DungeonSubscriptionPersistenceMapper.toEntity
import com.mnemos.notification.adapter.out.persistence.dungeon.DungeonSubscriptionPersistenceMapper.toEquivalenceKey
import com.mnemos.notification.adapter.out.persistence.subscription.SubscriptionPersistenceWriter
import com.mnemos.notification.domain.dungeon.model.DomainDungeonType
import com.mnemos.notification.domain.dungeon.model.DungeonSubscription
import com.mnemos.notification.domain.dungeon.repository.DungeonSubscriptionRepository
import com.mnemos.notification.domain.subscription.model.SubscriptionId
import org.springframework.stereotype.Repository

@Repository
class DungeonSubscriptionRepositoryAdapter(
    private val jpaRepository: DungeonSubscriptionJpaRepository,
    private val subscriptionPersistenceWriter: SubscriptionPersistenceWriter,
) : DungeonSubscriptionRepository {
    override fun createOrGetId(subscription: DungeonSubscription): SubscriptionId =
        subscriptionPersistenceWriter.createOrGetId(
            subscription = subscription,
            equivalenceKey = subscription.toEquivalenceKey(),
        ) {
            jpaRepository.save(subscription.toEntity())
        }

    override fun findMatchingUserIds(dungeonType: DomainDungeonType): Set<UserId> =
        jpaRepository
            .findMatchingUserIds(dungeonType)
            .mapTo(HashSet(), ::UserId)
}
