package com.mnemos.telegram.bot.application.menu

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuMessageRenderer
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuSessionRepository
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.message.model.TelegramMessageSendResult
import com.mnemos.telegram.bot.application.message.port.out.TelegramMessageGateway
import com.mnemos.telegram.bot.application.notification.port.out.NotificationRecipientGateway
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Service

@Service
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class TelegramMenuService(
    private val renderer: TelegramMenuMessageRenderer,
    private val screenDataResolver: TelegramMenuScreenDataResolver,
    private val messageGateway: TelegramMessageGateway,
    private val sessionRepository: TelegramMenuSessionRepository,
    private val recipientGateway: NotificationRecipientGateway,
) {
    fun open(
        telegramId: Long,
        screen: TelegramMenuScreen = TelegramMenuScreen.HOME,
    ): TelegramDeliveryResult {
        recipientGateway.activate(telegramId)

        val currentSession = sessionRepository.find(telegramId)
        val nextSession =
            TelegramMenuSession(
                telegramId = telegramId,
                menuMessageId = currentSession?.menuMessageId,
                currentScreen = screen,
            )

        return deliver(nextSession, currentSession?.menuMessageId)
    }

    fun navigate(
        telegramId: Long,
        messageId: Int,
        screen: TelegramMenuScreen,
    ): TelegramDeliveryResult {
        if (screen == TelegramMenuScreen.HOME) {
            return home(telegramId, messageId)
        }

        recipientGateway.activate(telegramId)

        val currentSession =
            sessionRepository.find(telegramId)
                ?: TelegramMenuSession(
                    telegramId = telegramId,
                    menuMessageId = messageId,
                )
        val navigationStack =
            if (currentSession.currentScreen == screen) {
                currentSession.navigationStack
            } else {
                (currentSession.navigationStack + currentSession.currentScreen).takeLast(MAX_NAVIGATION_DEPTH)
            }
        val nextSession =
            currentSession.copy(
                menuMessageId = messageId,
                currentScreen = screen,
                navigationStack = navigationStack,
                screenContext = emptyMap(),
            )

        return deliver(nextSession, messageId)
    }

    fun home(
        telegramId: Long,
        messageId: Int,
    ): TelegramDeliveryResult {
        recipientGateway.activate(telegramId)

        val nextSession =
            TelegramMenuSession(
                telegramId = telegramId,
                menuMessageId = messageId,
            )

        return deliver(nextSession, messageId)
    }

    fun back(
        telegramId: Long,
        messageId: Int,
    ): TelegramDeliveryResult {
        recipientGateway.activate(telegramId)

        val currentSession =
            sessionRepository.find(telegramId)
                ?: TelegramMenuSession(
                    telegramId = telegramId,
                    menuMessageId = messageId,
                )
        val previousScreen = currentSession.navigationStack.lastOrNull() ?: TelegramMenuScreen.HOME
        val nextStack =
            if (currentSession.navigationStack.isEmpty()) {
                emptyList()
            } else {
                currentSession.navigationStack.dropLast(1)
            }
        val nextSession =
            currentSession.copy(
                menuMessageId = messageId,
                currentScreen = previousScreen,
                navigationStack = nextStack,
                screenContext = emptyMap(),
            )

        return deliver(nextSession, messageId)
    }

    private fun deliver(
        session: TelegramMenuSession,
        preferredMessageId: Int?,
    ): TelegramDeliveryResult {
        val screenData = screenDataResolver.resolve(session)
        val message = renderer.render(session.currentScreen, screenData)

        if (preferredMessageId != null) {
            when (
                val editResult =
                    messageGateway.edit(
                        telegramId = session.telegramId,
                        messageId = preferredMessageId,
                        message = message,
                    )
            ) {
                TelegramDeliveryResult.Delivered -> {
                    sessionRepository.save(session.copy(menuMessageId = preferredMessageId))
                    return TelegramDeliveryResult.Delivered
                }

                TelegramDeliveryResult.Retryable -> return TelegramDeliveryResult.Retryable
                is TelegramDeliveryResult.PermanentFailure -> Unit
            }
        }

        return when (
            val sendResult =
                messageGateway.sendTracked(
                    telegramId = session.telegramId,
                    message = message,
                )
        ) {
            is TelegramMessageSendResult.Delivered -> {
                sessionRepository.save(session.copy(menuMessageId = sendResult.messageId))
                TelegramDeliveryResult.Delivered
            }

            TelegramMessageSendResult.Retryable -> TelegramDeliveryResult.Retryable
            is TelegramMessageSendResult.PermanentFailure ->
                TelegramDeliveryResult.PermanentFailure(sendResult.reason)
        }
    }

    private companion object {
        const val MAX_NAVIGATION_DEPTH = 16
    }
}
