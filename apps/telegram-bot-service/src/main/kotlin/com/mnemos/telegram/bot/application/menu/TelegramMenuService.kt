package com.mnemos.telegram.bot.application.menu

import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuMessageRenderer
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuSessionRepository
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.message.model.TelegramMessageSendResult
import com.mnemos.telegram.bot.application.message.port.out.TelegramMessageGateway
import com.mnemos.telegram.bot.application.notification.port.out.NotificationRecipientGateway
import com.mnemos.telegram.bot.application.settings.TelegramSettingsLanguageResolver
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
    private val languageResolver: TelegramSettingsLanguageResolver,
) {
    fun open(
        telegramId: Long,
        screen: TelegramMenuScreen = TelegramMenuScreen.HOME,
        screenContext: Map<String, String> = emptyMap(),
        pendingInput: TelegramPendingInput? = null,
    ): TelegramDeliveryResult {
        recipientGateway.activate(telegramId)

        val nextSession =
            TelegramMenuSession(
                telegramId = telegramId,
                menuMessageId = null,
                currentScreen = screen,
                pendingInput = pendingInput,
                screenContext = screenContext,
            )

        return deliver(nextSession, preferredMessageId = null)
    }

    fun navigate(
        telegramId: Long,
        messageId: Int,
        screen: TelegramMenuScreen,
    ): TelegramDeliveryResult {
        if (screen == TelegramMenuScreen.HOME) {
            return home(telegramId, messageId)
        }

        val currentSession = sessionRepository.find(telegramId)

        if (isStaleMenuMessage(currentSession, messageId)) {
            return TelegramDeliveryResult.Delivered
        }

        recipientGateway.activate(telegramId)

        val baseSession =
            currentSession
                ?: TelegramMenuSession(
                    telegramId = telegramId,
                    menuMessageId = messageId,
                )
        val navigationStack =
            if (baseSession.currentScreen == screen) {
                baseSession.navigationStack
            } else {
                (baseSession.navigationStack + baseSession.currentScreen).takeLast(MAX_NAVIGATION_DEPTH)
            }
        val nextSession =
            baseSession.copy(
                menuMessageId = messageId,
                currentScreen = screen,
                navigationStack = navigationStack,
                pendingInput = null,
                screenContext = emptyMap(),
            )

        return deliver(nextSession, messageId)
    }

    fun updateScreenContext(
        telegramId: Long,
        messageId: Int,
        expectedScreen: TelegramMenuScreen,
        update: (Map<String, String>) -> Map<String, String>,
    ): TelegramDeliveryResult =
        updateFromMenuMessage(
            telegramId = telegramId,
            messageId = messageId,
            expectedScreen = expectedScreen,
        ) { session ->
            session.copy(screenContext = update(session.screenContext))
        }

    fun requestTextInput(
        telegramId: Long,
        messageId: Int,
        expectedScreen: TelegramMenuScreen,
        pendingInput: TelegramPendingInput,
        updateContext: (Map<String, String>) -> Map<String, String> = { context -> context },
    ): TelegramDeliveryResult =
        updateFromMenuMessage(
            telegramId = telegramId,
            messageId = messageId,
            expectedScreen = expectedScreen,
        ) { session ->
            session.copy(
                pendingInput = pendingInput,
                screenContext = updateContext(session.screenContext),
            )
        }

    fun cancelTextInput(
        telegramId: Long,
        messageId: Int,
        expectedScreen: TelegramMenuScreen,
        updateContext: (Map<String, String>) -> Map<String, String> = { context -> context },
    ): TelegramDeliveryResult =
        updateFromMenuMessage(
            telegramId = telegramId,
            messageId = messageId,
            expectedScreen = expectedScreen,
        ) { session ->
            session.copy(
                pendingInput = null,
                screenContext = updateContext(session.screenContext),
            )
        }

    fun completeTextInput(
        telegramId: Long,
        expectedScreen: TelegramMenuScreen,
        expectedInput: TelegramPendingInput,
        updateContext: (Map<String, String>) -> Map<String, String>,
    ): TelegramDeliveryResult =
        updateStoredPendingInput(
            telegramId = telegramId,
            expectedScreen = expectedScreen,
            expectedInput = expectedInput,
        ) { session ->
            session.copy(
                pendingInput = null,
                screenContext = updateContext(session.screenContext),
            )
        }

    fun updatePendingTextInputContext(
        telegramId: Long,
        expectedScreen: TelegramMenuScreen,
        expectedInput: TelegramPendingInput,
        updateContext: (Map<String, String>) -> Map<String, String>,
    ): TelegramDeliveryResult =
        updateStoredPendingInput(
            telegramId = telegramId,
            expectedScreen = expectedScreen,
            expectedInput = expectedInput,
        ) { session ->
            session.copy(screenContext = updateContext(session.screenContext))
        }

    fun home(
        telegramId: Long,
        messageId: Int,
    ): TelegramDeliveryResult {
        val currentSession = sessionRepository.find(telegramId)

        if (isStaleMenuMessage(currentSession, messageId)) {
            return TelegramDeliveryResult.Delivered
        }

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
        val currentSession = sessionRepository.find(telegramId)

        if (isStaleMenuMessage(currentSession, messageId)) {
            return TelegramDeliveryResult.Delivered
        }

        recipientGateway.activate(telegramId)

        val baseSession =
            currentSession
                ?: TelegramMenuSession(
                    telegramId = telegramId,
                    menuMessageId = messageId,
                )
        val previousScreen = baseSession.navigationStack.lastOrNull() ?: TelegramMenuScreen.HOME
        val nextStack =
            if (baseSession.navigationStack.isEmpty()) {
                emptyList()
            } else {
                baseSession.navigationStack.dropLast(1)
            }
        val nextSession =
            baseSession.copy(
                menuMessageId = messageId,
                currentScreen = previousScreen,
                navigationStack = nextStack,
                pendingInput = null,
                screenContext = emptyMap(),
            )

        return deliver(nextSession, messageId)
    }

    private fun updateFromMenuMessage(
        telegramId: Long,
        messageId: Int,
        expectedScreen: TelegramMenuScreen,
        update: (TelegramMenuSession) -> TelegramMenuSession,
    ): TelegramDeliveryResult {
        val currentSession = sessionRepository.find(telegramId)

        if (isStaleMenuMessage(currentSession, messageId)) {
            return TelegramDeliveryResult.Delivered
        }

        if (currentSession != null && currentSession.currentScreen != expectedScreen) {
            return TelegramDeliveryResult.Delivered
        }

        recipientGateway.activate(telegramId)

        val baseSession =
            currentSession
                ?: TelegramMenuSession(
                    telegramId = telegramId,
                    menuMessageId = messageId,
                    currentScreen = expectedScreen,
                )
        val nextSession =
            update(baseSession).copy(
                menuMessageId = messageId,
                currentScreen = expectedScreen,
            )

        return deliver(nextSession, messageId)
    }

    private fun updateStoredPendingInput(
        telegramId: Long,
        expectedScreen: TelegramMenuScreen,
        expectedInput: TelegramPendingInput,
        update: (TelegramMenuSession) -> TelegramMenuSession,
    ): TelegramDeliveryResult {
        val currentSession =
            sessionRepository.find(telegramId)
                ?: return TelegramDeliveryResult.Delivered

        if (currentSession.currentScreen != expectedScreen || currentSession.pendingInput != expectedInput) {
            return TelegramDeliveryResult.Delivered
        }

        recipientGateway.activate(telegramId)

        return deliver(update(currentSession), currentSession.menuMessageId)
    }

    private fun deliver(
        session: TelegramMenuSession,
        preferredMessageId: Int?,
    ): TelegramDeliveryResult {
        val screenData = screenDataResolver.resolve(session)
        val language = languageResolver.resolve(session.telegramId)
        val message = renderer.render(session.currentScreen, screenData, language)

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

    private fun isStaleMenuMessage(
        currentSession: TelegramMenuSession?,
        messageId: Int,
    ): Boolean = currentSession?.menuMessageId?.let { storedMessageId -> storedMessageId != messageId } ?: false

    private companion object {
        const val MAX_NAVIGATION_DEPTH = 16
    }
}
