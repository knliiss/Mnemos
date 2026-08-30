package com.mnemos.telegram.bot.application.command.start

import com.mnemos.telegram.bot.application.command.port.out.TelegramCommandMessageRenderer
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.message.port.out.TelegramMessageGateway
import com.mnemos.telegram.bot.application.notification.port.out.NotificationRecipientGateway
import org.springframework.stereotype.Service

@Service
class HandleTelegramStartService(
    private val renderer: TelegramCommandMessageRenderer,
    private val messageGateway: TelegramMessageGateway,
    private val recipientGateway: NotificationRecipientGateway,
) {
    fun handle(context: TelegramStartContext): TelegramDeliveryResult {
        recipientGateway.activate(context.telegramId)

        return messageGateway.send(
            telegramId = context.telegramId,
            message = renderer.renderStart(context),
        )
    }
}
