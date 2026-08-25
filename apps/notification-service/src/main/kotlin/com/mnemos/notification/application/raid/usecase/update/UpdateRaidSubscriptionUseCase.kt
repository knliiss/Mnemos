package com.mnemos.notification.application.raid.usecase.update

interface UpdateRaidSubscriptionUseCase {
    fun execute(command: UpdateRaidSubscriptionCommand): UpdateRaidSubscriptionResult
}
