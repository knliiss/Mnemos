package com.mnemos.playerdirectory.application.subscription.usecase.list

import com.mnemos.playerdirectory.application.subscription.model.PresenceSubscriptionView
import java.util.UUID

fun interface ListPresenceSubscriptionsUseCase {
    fun execute(subscriberUserId: UUID): List<PresenceSubscriptionView>
}
