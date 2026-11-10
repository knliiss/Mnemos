package com.mnemos.telegram.bot.application.widget.input

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.message.model.TelegramMessageSendResult
import com.mnemos.telegram.bot.application.widget.input.port.out.WidgetTextInputSessionRepository
import com.mnemos.telegram.bot.application.widget.input.port.out.WidgetTextPromptGateway
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service
import org.slf4j.LoggerFactory
import java.util.UUID

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class StartWidgetTextInputService(
    private val properties: TelegramWidgetProperties,
    private val prompts: WidgetTextPromptGateway,
    private val sessions: WidgetTextInputSessionRepository,
) : StartWidgetTextInputUseCase {
    override fun start(session: TelegramWidgetSession, purpose: WidgetTextInputPurpose): TelegramDeliveryResult =
        startScoped(session, purpose, null)

    override fun startForRole(session: TelegramWidgetSession, roleId: UUID): TelegramDeliveryResult =
        startScoped(session, WidgetTextInputPurpose.ACCOUNT_ROLE_EDIT, roleId)

    private fun startScoped(
        session: TelegramWidgetSession,
        purpose: WidgetTextInputPurpose,
        roleId: UUID?,
    ): TelegramDeliveryResult {
        if (!properties.enabled || session.screen != purpose.screen ||
            (purpose == WidgetTextInputPurpose.ACCOUNT_ROLE_EDIT) != (roleId != null) ||
            purpose == WidgetTextInputPurpose.PROFILE_COMMENT_CREATE ||
            purpose == WidgetTextInputPurpose.PROFILE_COMMENT_REPLY ||
            purpose == WidgetTextInputPurpose.PROFILE_COMMENT_EDIT
        ) {
            return TelegramDeliveryResult.PermanentFailure("widget-input-unavailable")
        }

        return try {
            val result =
                when (val sent = prompts.prompt(session.telegramId, session.messageId, purpose)) {
                    is TelegramMessageSendResult.Delivered -> {
                        sessions.save(
                            WidgetTextInputSession(
                                telegramId = session.telegramId,
                                promptMessageId = sent.messageId,
                                widgetMessageId = session.messageId,
                                accountId = session.accountId,
                                purpose = purpose,
                                targetRoleId = roleId,
                            ),
                        )
                        TelegramDeliveryResult.Delivered
                    }
                    TelegramMessageSendResult.Retryable -> TelegramDeliveryResult.Retryable
                    is TelegramMessageSendResult.PermanentFailure -> TelegramDeliveryResult.PermanentFailure(sent.reason)
                }
            if (result != TelegramDeliveryResult.Delivered) {
                log.warn("Widget input prompt unavailable: purpose={}, outcome={}", purpose, result.javaClass.simpleName)
            }
            result
        } catch (exception: Exception) {
            log.warn("Widget input prompt failed: purpose={}", purpose, exception)
            TelegramDeliveryResult.Retryable
        }
    }

    private companion object {
        val log = LoggerFactory.getLogger(StartWidgetTextInputService::class.java)
    }
}
