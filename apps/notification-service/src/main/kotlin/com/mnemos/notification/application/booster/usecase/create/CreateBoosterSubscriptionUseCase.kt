package com.mnemos.notification.application.booster.usecase.create

interface CreateBoosterSubscriptionUseCase {
    fun execute(command: CreateBoosterSubscriptionCommand): CreateBoosterSubscriptionResult
}
