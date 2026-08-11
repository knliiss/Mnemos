package com.mnemos.notification.application.notification.delivery.model

import java.time.Duration

sealed interface DeliveryResult {
    data object Delivered : DeliveryResult

    data class Retryable(
        val retryAfter: Duration? = null,
    ) : DeliveryResult

    data class PermanentFailure(
        val reason: String,
    ) : DeliveryResult
}
