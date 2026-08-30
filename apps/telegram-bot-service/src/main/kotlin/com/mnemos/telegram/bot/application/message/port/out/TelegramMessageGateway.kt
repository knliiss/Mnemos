package com.mnemos.telegram.bot.application.message.port.out

import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult

interface TelegramMessageGateway {
    fun send(
        telegramId: Long,
        message: OutboundTelegramMessage,
    ): TelegramDeliveryResult

    fun edit(
        telegramId: Long,
        messageId: Int,
        message: OutboundTelegramMessage,
    ): TelegramDeliveryResult
}
