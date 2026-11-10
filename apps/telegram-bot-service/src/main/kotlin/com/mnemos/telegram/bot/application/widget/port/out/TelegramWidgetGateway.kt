package com.mnemos.telegram.bot.application.widget.port.out

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetMessage
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetSendResult

interface TelegramWidgetGateway {
    fun sendTracked(
        telegramId: Long,
        widget: TelegramWidgetMessage,
    ): TelegramWidgetSendResult

    fun answerQuery(
        queryId: String,
        data: String,
    ): TelegramDeliveryResult

    fun updateChatData(
        telegramId: Long,
        messageId: Int,
        data: String,
    ): TelegramDeliveryResult

    fun send(
        telegramId: Long,
        widget: TelegramWidgetMessage,
    ): TelegramDeliveryResult =
        when (val result = sendTracked(telegramId, widget)) {
            is TelegramWidgetSendResult.Sent -> TelegramDeliveryResult.Delivered
            TelegramWidgetSendResult.Retryable -> TelegramDeliveryResult.Retryable
            is TelegramWidgetSendResult.PermanentFailure -> TelegramDeliveryResult.PermanentFailure(result.reason)
        }
}
