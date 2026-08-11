package com.mnemos.notification.application.subscription.usecase.delete

fun interface DeleteSubscriptionUseCase {
    fun execute(command: DeleteSubscriptionCommand)
}
