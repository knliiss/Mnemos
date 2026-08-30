package com.mnemos.telegram.bot.application.online

import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.callback.TelegramScopedCallbackHandler
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class SaoOnlineCallbackHandler(
    private val menuService: TelegramMenuService,
) : TelegramScopedCallbackHandler {
    override val callbackPrefix: String = SaoOnlineCallbackData.PREFIX

    override fun handle(context: TelegramCallbackContext): TelegramDeliveryResult =
        when (context.data) {
            SaoOnlineCallbackData.REFRESH -> updateContext(context) { current -> current }
            SaoOnlineCallbackData.REALM_LIST -> updateContext(context) { emptyMap() }
            SaoOnlineCallbackData.PREVIOUS_PAGE -> changePage(context, -1)
            SaoOnlineCallbackData.NEXT_PAGE -> changePage(context, 1)
            else -> openRealm(context)
        }

    private fun openRealm(context: TelegramCallbackContext): TelegramDeliveryResult {
        val realmNumber = SaoOnlineCallbackData.realmNumber(context.data)
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
            if (current[SaoOnlineMenuContext.MODE_KEY] != SaoOnlineMenuContext.MODE_REALM) {
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
}
