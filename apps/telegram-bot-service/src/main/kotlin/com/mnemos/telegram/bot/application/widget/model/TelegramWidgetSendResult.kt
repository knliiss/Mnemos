package com.mnemos.telegram.bot.application.widget.model

sealed interface TelegramWidgetSendResult {
    data class Sent(
        val messageId: Int,
    ) : TelegramWidgetSendResult

    data object Retryable : TelegramWidgetSendResult

    data class PermanentFailure(
        val reason: String,
    ) : TelegramWidgetSendResult
}
