package com.mnemos.telegram.bot.application.command.help

import com.mnemos.telegram.bot.application.command.port.out.TelegramCommandMessageRenderer
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.message.port.out.TelegramMessageGateway
import com.mnemos.telegram.bot.application.notification.port.out.NotificationRecipientGateway
import com.mnemos.telegram.bot.application.settings.TelegramSettingsLanguageResolver
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class HandleTelegramHelpService(
    private val renderer: TelegramCommandMessageRenderer,
    private val messageGateway: TelegramMessageGateway,
    private val recipientGateway: NotificationRecipientGateway,
    private val languageResolver: TelegramSettingsLanguageResolver,
) {
    fun send(
        telegramId: Long,
        section: TelegramHelpSection = TelegramHelpSection.OVERVIEW,
    ): TelegramDeliveryResult {
        recipientGateway.activate(telegramId)

        val language = languageResolver.resolve(telegramId)

        return messageGateway.send(
            telegramId = telegramId,
            message = renderer.renderHelp(section, language),
        )
    }

    fun switchSection(
        telegramId: Long,
        messageId: Int,
        section: TelegramHelpSection,
    ): TelegramDeliveryResult {
        recipientGateway.activate(telegramId)

        val language = languageResolver.resolve(telegramId)

        return messageGateway.edit(
            telegramId = telegramId,
            messageId = messageId,
            message = renderer.renderHelp(section, language),
        )
    }
}
