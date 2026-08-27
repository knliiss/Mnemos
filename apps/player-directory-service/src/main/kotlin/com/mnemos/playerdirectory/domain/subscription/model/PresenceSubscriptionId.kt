package com.mnemos.playerdirectory.domain.subscription.model

import com.github.f4b6a3.uuid.UuidCreator
import java.util.UUID

@JvmInline
value class PresenceSubscriptionId(
    val id: UUID,
) {
    companion object {
        fun generate(): PresenceSubscriptionId = PresenceSubscriptionId(UuidCreator.getTimeOrderedEpoch())
    }
}
