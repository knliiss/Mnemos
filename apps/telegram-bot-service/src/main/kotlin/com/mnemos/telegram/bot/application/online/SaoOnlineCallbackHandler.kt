package com.mnemos.telegram.bot.application.online

import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.callback.TelegramScopedCallbackHandler
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuSession
import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import com.mnemos.telegram.bot.application.menu.port.out.TelegramMenuSessionRepository
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class SaoOnlineCallbackHandler(
    private val menuService: TelegramMenuService,
    private val sessionRepository: TelegramMenuSessionRepository,
) : TelegramScopedCallbackHandler {
    override val callbackPrefix: String = SaoOnlineCallbackData.PREFIX

    override fun handle(context: TelegramCallbackContext): TelegramDeliveryResult {
        val session = currentOnlineSession(context) ?: return TelegramDeliveryResult.Delivered

        if (session.pendingInput == TelegramPendingInput.ONLINE_PLAYER_SEARCH) {
            return if (context.data == SaoOnlineCallbackData.SEARCH_CANCEL) {
                cancelSearch(context)
            } else {
                TelegramDeliveryResult.Delivered
            }
        }

        return when (context.data) {
            SaoOnlineCallbackData.REFRESH -> updateContext(context) { current -> current }
            SaoOnlineCallbackData.REALM_LIST -> updateContext(context) { emptyMap() }
            SaoOnlineCallbackData.PREVIOUS_PAGE -> changePage(context, -1)
            SaoOnlineCallbackData.NEXT_PAGE -> changePage(context, 1)
            SaoOnlineCallbackData.SEARCH -> startSearch(context)
            SaoOnlineCallbackData.SEARCH_CANCEL -> TelegramDeliveryResult.Delivered
            else -> openRealm(context)
        }
    }

    private fun startSearch(context: TelegramCallbackContext): TelegramDeliveryResult =
        menuService.requestTextInput(
            telegramId = context.telegramId,
            messageId = context.messageId,
            expectedScreen = TelegramMenuScreen.ONLINE,
            pendingInput = TelegramPendingInput.ONLINE_PLAYER_SEARCH,
            updateContext = { current -> current - SaoOnlineMenuContext.SEARCH_ERROR_KEY },
        )

    private fun cancelSearch(context: TelegramCallbackContext): TelegramDeliveryResult =
        menuService.cancelTextInput(
            telegramId = context.telegramId,
            messageId = context.messageId,
            expectedScreen = TelegramMenuScreen.ONLINE,
        )

    private fun openRealm(context: TelegramCallbackContext): TelegramDeliveryResult {
        val realmNumber =
            SaoOnlineCallbackData.realmNumber(context.data)
                ?: return TelegramDeliveryResult.Delivered

        return updateContext(context) {
            SaoOnlineMenuContext.realm(realmNumber)
        }
    }

    private fun changePage(
        context: TelegramCallbackContext,
        delta: Int,
    ): TelegramDeliveryResult =
        updateContext(context) { current ->
            val mode = current[SaoOnlineMenuContext.MODE_KEY]

            if (mode != SaoOnlineMenuContext.MODE_REALM && mode != SaoOnlineMenuContext.MODE_SEARCH) {
                current
            } else {
                val page = current[SaoOnlineMenuContext.PAGE_KEY]?.toIntOrNull() ?: 0

                current + (SaoOnlineMenuContext.PAGE_KEY to maxOf(0, page + delta).toString())
            }
        }

    private fun updateContext(
        context: TelegramCallbackContext,
        update: (Map<String, String>) -> Map<String, String>,
    ): TelegramDeliveryResult =
        menuService.updateScreenContext(
            telegramId = context.telegramId,
            messageId = context.messageId,
            expectedScreen = TelegramMenuScreen.ONLINE,
            update = update,
        )

    private fun currentOnlineSession(context: TelegramCallbackContext): TelegramMenuSession? {
        val session = sessionRepository.find(context.telegramId) ?: return null

        if (session.currentScreen != TelegramMenuScreen.ONLINE || session.menuMessageId != context.messageId) {
            return null
        }

        return session
    }
}
