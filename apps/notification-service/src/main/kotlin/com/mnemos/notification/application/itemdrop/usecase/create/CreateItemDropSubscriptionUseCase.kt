package com.mnemos.notification.application.itemdrop.usecase.create

interface CreateItemDropSubscriptionUseCase {
    fun execute(command: CreateItemDropSubscriptionCommand): CreateItemDropSubscriptionResult
}
