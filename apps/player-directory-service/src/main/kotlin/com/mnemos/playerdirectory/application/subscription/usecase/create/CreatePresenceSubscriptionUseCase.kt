package com.mnemos.playerdirectory.application.subscription.usecase.create

import com.mnemos.playerdirectory.domain.subscription.model.PresenceSubscription

fun interface CreatePresenceSubscriptionUseCase {
    fun execute(command: CreatePresenceSubscriptionCommand): PresenceSubscription
}
