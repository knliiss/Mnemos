package com.mnemos.notification.adapter.out.persistence.subscription

import com.mnemos.notification.domain.subscription.model.Subscription
import com.mnemos.notification.domain.subscription.model.SubscriptionId
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class SubscriptionPersistenceWriter(
    private val subscriptionJpaRepository: SubscriptionJpaRepository,
) {
    fun createOrGetId(
        subscription: Subscription,
        equivalenceKey: String,
        saveSubtype: () -> Unit,
    ): SubscriptionId {
        val inserted =
            subscriptionJpaRepository.insertIfAbsent(
                id = subscription.id.id,
                userId = subscription.userId.id,
                type = subscription.type.name,
                equivalenceKey = equivalenceKey,
                createdAt = Instant.now(),
            )

        if (inserted == 0) {
            val existingId =
                checkNotNull(
                    subscriptionJpaRepository.findEquivalentId(
                        userId = subscription.userId.id,
                        type = subscription.type,
                        equivalenceKey = equivalenceKey,
                    ),
                ) {
                    "Equivalent subscription not found after insert conflict"
                }

            return SubscriptionId(existingId)
        }

        saveSubtype()
        return subscription.id
    }
}
