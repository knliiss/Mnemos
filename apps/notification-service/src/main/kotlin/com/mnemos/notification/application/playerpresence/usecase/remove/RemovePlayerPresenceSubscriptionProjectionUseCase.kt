package com.mnemos.notification.application.playerpresence.usecase.remove

import java.util.UUID

fun interface RemovePlayerPresenceSubscriptionProjectionUseCase {
    fun execute(subscriptionId: UUID)
}
