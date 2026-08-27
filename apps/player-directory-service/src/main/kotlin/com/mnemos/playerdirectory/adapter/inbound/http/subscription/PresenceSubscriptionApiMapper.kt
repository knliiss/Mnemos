package com.mnemos.playerdirectory.adapter.inbound.http.subscription

import com.mnemos.playerdirectory.application.subscription.model.PresenceSubscriptionView
import com.mnemos.playerdirectory.domain.subscription.model.PresenceSubscription

object PresenceSubscriptionApiMapper {
    fun PresenceSubscription.toResponse(): PresenceSubscriptionResponse =
        PresenceSubscriptionResponse(
            id = id.id,
            playerId = playerId.id,
            nickname = null,
            presenceStatus = null,
            realm = null,
            lastSeenOnline = null,
            createdAt = createdAt,
        )

    fun PresenceSubscriptionView.toResponse(): PresenceSubscriptionResponse =
        PresenceSubscriptionResponse(
            id = id.id,
            playerId = playerId.id,
            nickname = nickname,
            presenceStatus = presenceStatus.name,
            realm = realm,
            lastSeenOnline = lastSeenOnline,
            createdAt = createdAt,
        )
}
