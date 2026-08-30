package com.mnemos.telegram.bot.application.payment

object PaymentServiceFailurePolicy {
    fun isPermanent(status: Int): Boolean = status in 400..499 && status != 408 && status != 429
}
