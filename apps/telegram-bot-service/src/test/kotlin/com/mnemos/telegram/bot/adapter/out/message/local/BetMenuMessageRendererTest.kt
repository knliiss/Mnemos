package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.bet.BetCallbackData
import com.mnemos.telegram.bot.application.bet.model.BetMenuFeedback
import com.mnemos.telegram.bot.application.bet.model.BetMenuMode
import com.mnemos.telegram.bot.application.bet.model.BetMenuScreenData
import com.mnemos.telegram.bot.application.bet.model.TelegramBetRoundsResult
import com.mnemos.telegram.bot.application.bet.model.TelegramItemRacesResult
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class BetMenuMessageRendererTest {
    @Test
    fun `tracked operation hides new bet and mode actions until refresh resolves it`() {
        val message =
            BetMenuMessageRenderer.render(
                TelegramMenuScreenData.Bets(
                    BetMenuScreenData(
                        mode = BetMenuMode.ROUNDS,
                        rounds = TelegramBetRoundsResult.Available(emptyList()),
                        eventHistory = null,
                        races = TelegramItemRacesResult.Available(emptyList()),
                        raceHistory = null,
                        selectedRoundId = null,
                        selectedRaceId = null,
                        selectedSide = null,
                        feedback = BetMenuFeedback.PENDING,
                        waitingForAmount = false,
                        increasing = false,
                        operationTracked = true,
                    ),
                ),
            )
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

        assertTrue(message.text.contains("Операция ещё обрабатывается"))
        assertTrue(message.text.contains("Нажмите «Обновить»"))
        assertEquals(listOf(1, 1), message.actionRows.map(List<*>::size))
        assertTrue(callbacks.any { action -> action.data == BetCallbackData.REFRESH })
        assertTrue(callbacks.any { action -> action.data == TelegramMenuScreen.BACK_CALLBACK_DATA })
        assertFalse(callbacks.any { action -> action.data == BetCallbackData.ROUNDS })
        assertFalse(callbacks.any { action -> action.data == BetCallbackData.RACES })
        assertFalse(callbacks.any { action -> action.data == BetCallbackData.HISTORY })
        assertFalse(callbacks.any { action -> action.data.startsWith("bet:place:") })
        assertFalse(callbacks.any { action -> action.data.startsWith("bet:race:") })
    }
}
