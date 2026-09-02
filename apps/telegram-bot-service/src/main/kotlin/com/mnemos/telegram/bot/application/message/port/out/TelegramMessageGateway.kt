package com.mnemos.telegram.bot.application.message.port.out

import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.message.model.TelegramMessageSendResult

interface TelegramMessageGateway {
    fun sendTracked(
        telegramId: Long,
        message: OutboundTelegramMessage,
    ): TelegramMessageSendResult

    fun send(
        telegramId: Long,
        message: OutboundTelegramMessage,
    ): TelegramDeliveryResult = sendTracked(telegramId, message).toDeliveryResult()

    fun edit(
        telegramId: Long,
        messageId: Int,
        message: OutboundTelegramMessage,
    ): TelegramDeliveryResult
}
