package com.mnemos.telegram.bot.application.payment

internal object PaymentServiceFailurePolicy {
    private val retryableClientStatuses = setOf(401, 403, 408, 425, 429)

    fun isPermanent(status: Int): Boolean = status in 400..499 && status !in retryableClientStatuses
}
