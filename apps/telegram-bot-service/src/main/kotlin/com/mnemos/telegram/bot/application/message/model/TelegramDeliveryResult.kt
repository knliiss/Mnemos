package com.mnemos.telegram.bot.application.message.model

sealed interface TelegramDeliveryResult {
    data object Delivered : TelegramDeliveryResult

    data object Retryable : TelegramDeliveryResult

    data class PermanentFailure(
        val reason: String,
    ) : TelegramDeliveryResult
}
