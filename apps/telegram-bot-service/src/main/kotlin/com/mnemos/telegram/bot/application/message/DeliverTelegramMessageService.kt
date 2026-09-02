package com.mnemos.telegram.bot.application.message

import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessage
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.message.port.out.TelegramMessageGateway
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class DeliverTelegramMessageService(
    private val gateway: TelegramMessageGateway,
) {
    fun send(
        telegramId: Long,
        message: OutboundTelegramMessage,
    ): TelegramDeliveryResult = gateway.send(telegramId, message)

    fun edit(
        telegramId: Long,
        messageId: Int,
        message: OutboundTelegramMessage,
    ): TelegramDeliveryResult = gateway.edit(telegramId, messageId, message)
}
