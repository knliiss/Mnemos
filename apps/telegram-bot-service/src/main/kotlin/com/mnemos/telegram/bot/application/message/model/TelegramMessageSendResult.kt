package com.mnemos.telegram.bot.application.message.model

sealed interface TelegramMessageSendResult {
    fun toDeliveryResult(): TelegramDeliveryResult =
        when (this) {
            is Delivered -> TelegramDeliveryResult.Delivered
            Retryable -> TelegramDeliveryResult.Retryable
            is PermanentFailure -> TelegramDeliveryResult.PermanentFailure(reason)
        }

    data class Delivered(
        val messageId: Int,
    ) : TelegramMessageSendResult

    data object Retryable : TelegramMessageSendResult

    data class PermanentFailure(
        val reason: String,
    ) : TelegramMessageSendResult
}
