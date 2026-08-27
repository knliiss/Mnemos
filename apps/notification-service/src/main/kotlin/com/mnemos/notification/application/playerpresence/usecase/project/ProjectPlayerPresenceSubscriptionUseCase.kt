package com.mnemos.notification.application.playerpresence.usecase.project

import com.mnemos.notification.domain.playerpresence.model.PlayerPresenceSubscription

fun interface ProjectPlayerPresenceSubscriptionUseCase {
    fun execute(subscription: PlayerPresenceSubscription)
}
