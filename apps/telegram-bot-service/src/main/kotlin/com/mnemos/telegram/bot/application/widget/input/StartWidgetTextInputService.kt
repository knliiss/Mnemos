package com.mnemos.telegram.bot.application.widget.input

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.message.model.TelegramMessageSendResult
import com.mnemos.telegram.bot.application.widget.input.port.out.WidgetTextInputSessionRepository
import com.mnemos.telegram.bot.application.widget.input.port.out.WidgetTextPromptGateway
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class StartWidgetTextInputService(
    private val properties: TelegramWidgetProperties,
    private val prompts: WidgetTextPromptGateway,
    private val sessions: WidgetTextInputSessionRepository,
) : StartWidgetTextInputUseCase {
    override fun start(session: TelegramWidgetSession, purpose: WidgetTextInputPurpose): TelegramDeliveryResult {
        if (!properties.enabled || session.screen != purpose.screen) {
            return TelegramDeliveryResult.PermanentFailure("widget-input-unavailable")
        }

        return when (val sent = prompts.prompt(session.telegramId, session.messageId, purpose)) {
            is TelegramMessageSendResult.Delivered -> {
                sessions.save(
                    WidgetTextInputSession(
                        telegramId = session.telegramId,
                        promptMessageId = sent.messageId,
                        widgetMessageId = session.messageId,
                        accountId = session.accountId,
                        purpose = purpose,
                    ),
                )
                TelegramDeliveryResult.Delivered
            }
            TelegramMessageSendResult.Retryable -> TelegramDeliveryResult.Retryable
            is TelegramMessageSendResult.PermanentFailure -> TelegramDeliveryResult.PermanentFailure(sent.reason)
        }
    }
}
