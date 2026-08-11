package com.mnemos.notification.adapter.out.persistence.subscription

import com.mnemos.common.core.model.id.UserId
import com.mnemos.notification.domain.subscription.model.SubscriptionId
import com.mnemos.notification.domain.subscription.repository.SubscriptionRepository
import org.springframework.stereotype.Repository

@Repository
class SubscriptionRepositoryAdapter(
    private val subscriptionJpaRepository: SubscriptionJpaRepository,
) : SubscriptionRepository {
    override fun deleteByIdAndUserId(
        subscriptionId: SubscriptionId,
        userId: UserId,
    ): Boolean =
        subscriptionJpaRepository.deleteOwnedById(
            id = subscriptionId.id,
            userId = userId.id,
        ) == 1
}
