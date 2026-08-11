package com.mnemos.notification.application.raid.usecase.create

interface CreateRaidSubscriptionUseCase {
    fun execute(command: CreateRaidSubscriptionCommand): CreateRaidSubscriptionResult
}
