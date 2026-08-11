package com.mnemos.notification.domain.subscription.model

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

@JvmInline
value class SubscriptionId(
    val id: UUID,
) {
    companion object {
        fun generate(): SubscriptionId = SubscriptionId(UuidCreator.getTimeOrderedEpoch())
    }
}
