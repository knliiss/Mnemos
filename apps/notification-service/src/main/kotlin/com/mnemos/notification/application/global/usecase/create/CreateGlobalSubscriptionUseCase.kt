package com.mnemos.notification.application.global.usecase.create

interface CreateGlobalSubscriptionUseCase {
    fun execute(command: CreateGlobalSubscriptionCommand): CreateGlobalSubscriptionResult
}
