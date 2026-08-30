package com.mnemos.telegram.bot.application.online

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
class SaoOnlinePlayerSearchTextInputHandler(
    private val menuService: TelegramMenuService,
) : TelegramTextInputHandler {
    override val pendingInput: TelegramPendingInput = TelegramPendingInput.ONLINE_PLAYER_SEARCH

    override fun handle(context: TelegramTextInputContext): TelegramDeliveryResult {
        val query = context.text.trim()

        if (!isValidQuery(query)) {
            return menuService.updatePendingTextInputContext(
                telegramId = context.telegramId,
                expectedScreen = TelegramMenuScreen.ONLINE,
                expectedInput = pendingInput,
            ) { current ->
                current + (SaoOnlineMenuContext.SEARCH_ERROR_KEY to INVALID_QUERY_MESSAGE)
            }
        }

        return menuService.completeTextInput(
            telegramId = context.telegramId,
            expectedScreen = TelegramMenuScreen.ONLINE,
            expectedInput = pendingInput,
        ) {
            SaoOnlineMenuContext.search(query)
        }
    }

    private fun isValidQuery(query: String): Boolean =
        query.length in MIN_QUERY_LENGTH..MAX_QUERY_LENGTH && query.none { character -> character.isWhitespace() }

    private companion object {
        const val MIN_QUERY_LENGTH = 2
        const val MAX_QUERY_LENGTH = 32
        const val INVALID_QUERY_MESSAGE = "Введите от 2 до 32 символов без пробелов."
    }
}
