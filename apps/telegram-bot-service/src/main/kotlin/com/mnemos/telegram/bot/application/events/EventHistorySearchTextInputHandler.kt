package com.mnemos.telegram.bot.application.events

import com.mnemos.telegram.bot.application.input.TelegramTextInputContext
import com.mnemos.telegram.bot.application.input.TelegramTextInputHandler
import com.mnemos.telegram.bot.application.menu.TelegramMenuService
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramPendingInput
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty
import org.springframework.stereotype.Component

@Component
@ConditionalOnProperty(prefix = "mnemos.telegram-bot.telegram", name = ["enabled"], havingValue = "true")
class EventHistorySearchTextInputHandler(
    private val menuService: TelegramMenuService,
) : TelegramTextInputHandler {
    override val pendingInput: TelegramPendingInput = TelegramPendingInput.EVENT_SEARCH

    override fun handle(context: TelegramTextInputContext): TelegramDeliveryResult {
        val query = EventHistorySearchQuery.normalize(context.text)

        if (query == null) {
            return menuService.updatePendingTextInputContext(
                telegramId = context.telegramId,
                expectedScreen = TelegramMenuScreen.EVENTS,
                expectedInput = pendingInput,
            ) { current ->
                EventHistoryMenuContext.withSearchError(current, EventHistorySearchQuery.INVALID_MESSAGE)
            }
        }

        return menuService.completeTextInput(
            telegramId = context.telegramId,
            expectedScreen = TelegramMenuScreen.EVENTS,
            expectedInput = pendingInput,
        ) { current ->
            EventHistoryMenuContext.search(current, query)
        }
    }
}
