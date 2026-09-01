package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.bet.BetCallbackData
import com.mnemos.telegram.bot.application.bet.model.BetMenuFeedback
import com.mnemos.telegram.bot.application.bet.model.BetMenuMode
import com.mnemos.telegram.bot.application.bet.model.BetMenuScreenData
import com.mnemos.telegram.bot.application.bet.model.TelegramBetHistoryResult
import com.mnemos.telegram.bot.application.bet.model.TelegramBetRoundsResult
import com.mnemos.telegram.bot.application.bet.model.TelegramBetSide
import com.mnemos.telegram.bot.application.bet.model.TelegramEventBet
import com.mnemos.telegram.bot.application.bet.model.TelegramItemRaceHistoryResult
import com.mnemos.telegram.bot.application.bet.model.TelegramItemRacePosition
import com.mnemos.telegram.bot.application.bet.model.TelegramItemRacesResult
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.UUID

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

    @Test
    fun `history renders user statuses without backend ids`() {
        val roundId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71")
        val raceId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b72")
        val eventBet =
            TelegramEventBet(
                id = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b73"),
                roundId = roundId,
                side = TelegramBetSide.YES,
                confirmedAmount = 250,
                pendingAmount = 50,
                status = "SETTLEMENT_PENDING",
            )
        val racePosition =
            TelegramItemRacePosition(
                id = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b74"),
                raceId = raceId,
                accountNickname = "knaliz",
                confirmedAmount = 700,
                pendingAmount = 0,
                status = "WON",
            )
        val message =
            BetMenuMessageRenderer.render(
                TelegramMenuScreenData.Bets(
                    BetMenuScreenData(
                        mode = BetMenuMode.HISTORY,
                        rounds = TelegramBetRoundsResult.Available(emptyList()),
                        eventHistory = TelegramBetHistoryResult.Available(listOf(eventBet)),
                        races = TelegramItemRacesResult.Available(emptyList()),
                        raceHistory = TelegramItemRaceHistoryResult.Available(listOf(racePosition)),
                        selectedRoundId = null,
                        selectedRaceId = null,
                        selectedSide = null,
                        feedback = null,
                        waitingForAmount = false,
                        increasing = false,
                    ),
                ),
            )

        assertTrue(message.text.contains("Статус: <b>ожидает результата</b>"))
        assertTrue(message.text.contains("Статус: <b>выигрыш</b>"))
        assertTrue(message.text.contains("ожидает подтверждения: 50 COIN"))
        assertFalse(message.text.contains(roundId.toString().take(8)))
        assertFalse(message.text.contains(raceId.toString().take(8)))
        assertFalse(message.text.contains("SETTLEMENT_PENDING"))
        assertFalse(message.text.contains("WON"))
    }
}
