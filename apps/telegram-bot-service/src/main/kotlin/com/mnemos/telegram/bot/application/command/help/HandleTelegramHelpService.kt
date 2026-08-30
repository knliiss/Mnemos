package com.mnemos.telegram.bot.application.command.help

import com.mnemos.telegram.bot.application.command.port.out.TelegramCommandMessageRenderer
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.message.port.out.TelegramMessageGateway
import com.mnemos.telegram.bot.application.notification.port.out.NotificationRecipientGateway
import org.springframework.stereotype.Service

@Service
class HandleTelegramHelpService(
    private val renderer: TelegramCommandMessageRenderer,
    private val messageGateway: TelegramMessageGateway,
    private val recipientGateway: NotificationRecipientGateway,
) {
    fun send(
        telegramId: Long,
        section: TelegramHelpSection = TelegramHelpSection.OVERVIEW,
    ): TelegramDeliveryResult {
        recipientGateway.activate(telegramId)

        return messageGateway.send(
            telegramId = telegramId,
            message = renderer.renderHelp(section),
        )
    }

    fun switchSection(
        telegramId: Long,
        messageId: Int,
        section: TelegramHelpSection,
    ): TelegramDeliveryResult {
        recipientGateway.activate(telegramId)

        return messageGateway.edit(
            telegramId = telegramId,
            messageId = messageId,
            message = renderer.renderHelp(section),
        )
    }
}
