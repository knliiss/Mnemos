package com.mnemos.telegram.bot.application.events

import com.mnemos.telegram.bot.application.callback.TelegramCallbackContext
import com.mnemos.telegram.bot.application.callback.TelegramScopedCallbackHandler
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class EventHistoryCallbackHandler(
    private val menuService: TelegramMenuService,
) : TelegramScopedCallbackHandler {
    override val callbackPrefix: String = EventHistoryCallbackData.PREFIX

    override fun handle(context: TelegramCallbackContext): TelegramDeliveryResult =
        when (context.data) {
            EventHistoryCallbackData.REFRESH -> updateContext(context) { current -> current }
            EventHistoryCallbackData.PREVIOUS -> updateContext(context, EventHistoryMenuContext::previous)
            else -> handleParameterizedCallback(context)
        }

    private fun handleParameterizedCallback(context: TelegramCallbackContext): TelegramDeliveryResult {
        EventHistoryCallbackData.selectedFilter(context.data)?.let { filter ->
            return updateContext(context) {
                EventHistoryMenuContext.filter(filter)
            }
        }

        EventHistoryCallbackData.nextCursor(context.data)?.let { cursor ->
            return updateContext(context) { current ->
                EventHistoryMenuContext.next(current, cursor)
            }
        }

        return TelegramDeliveryResult.Delivered
    }

    private fun updateContext(
        context: TelegramCallbackContext,
        update: (Map<String, String>) -> Map<String, String>,
    ): TelegramDeliveryResult =
        menuService.updateScreenContext(
            telegramId = context.telegramId,
            messageId = context.messageId,
            expectedScreen = TelegramMenuScreen.EVENTS,
            update = update,
        )
}
