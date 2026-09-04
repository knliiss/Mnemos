package com.mnemos.telegram.bot.application.command.start

import com.mnemos.telegram.bot.application.command.port.out.TelegramCommandMessageRenderer
import com.mnemos.telegram.bot.application.identity.port.out.TelegramUserIdentityGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.message.port.out.TelegramMessageGateway
import com.mnemos.telegram.bot.application.notification.port.out.NotificationRecipientGateway
import com.mnemos.telegram.bot.application.settings.TelegramSettingsLanguageResolver
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class HandleTelegramStartService(
    private val renderer: TelegramCommandMessageRenderer,
    private val messageGateway: TelegramMessageGateway,
    private val recipientGateway: NotificationRecipientGateway,
    private val userIdentityGateway: TelegramUserIdentityGateway,
    private val languageResolver: TelegramSettingsLanguageResolver,
) {
    fun handle(context: TelegramStartContext): TelegramDeliveryResult {
        val identityReady =
            userIdentityGateway.bootstrap(
                telegramId = context.telegramId,
                username = context.username,
            )

        if (!identityReady) {
            return TelegramDeliveryResult.Retryable
        }

        recipientGateway.activate(context.telegramId)

        val language = languageResolver.resolve(context.telegramId)

        return messageGateway.send(
            telegramId = context.telegramId,
            message = renderer.renderStart(context, language),
        )
    }
}
