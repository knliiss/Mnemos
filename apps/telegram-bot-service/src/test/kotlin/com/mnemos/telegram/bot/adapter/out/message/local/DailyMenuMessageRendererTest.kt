package com.mnemos.telegram.bot.adapter.out.message.local

import com.mnemos.telegram.bot.application.daily.DailyCallbackData
import com.mnemos.telegram.bot.application.daily.model.DailyMenuFeedback
import com.mnemos.telegram.bot.application.daily.model.DailyMenuFeedbackType
import com.mnemos.telegram.bot.application.daily.model.DailyMenuScreenData
import com.mnemos.telegram.bot.application.daily.model.DailyRewardState
import com.mnemos.telegram.bot.application.daily.model.DailyRewardStateResult
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreen
import com.mnemos.telegram.bot.application.menu.model.TelegramMenuScreenData
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramButtonStyle
import com.mnemos.telegram.bot.application.message.model.OutboundTelegramMessageAction
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class DailyMenuMessageRendererTest {
    @Test
    fun `available daily renders structured reward state and full width actions`() {
        val data =
            TelegramMenuScreenData.Daily(
                DailyMenuScreenData(
                    result = DailyRewardStateResult.Available(recoverableState()),
                    feedback = null,
                ),
            )

        val message = DailyMenuMessageRenderer.render(data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()
        val claimAction = callbacks.single { action -> action.data == DailyCallbackData.CLAIM }
        val restoreAction = callbacks.single { action -> action.data == DailyCallbackData.RESTORE }

        assertTrue(message.text.contains("🔥 <b>Серия</b>"))
        assertTrue(message.text.contains("└ 7 дней"))
        assertTrue(message.text.contains("├ 70–90 COIN"))
        assertTrue(message.text.contains("└ PRIME: 0.20%"))
        assertTrue(message.text.contains("Серия для восстановления: 12"))
        assertEquals(OutboundTelegramButtonStyle.SUCCESS, claimAction.style)
        assertEquals(OutboundTelegramButtonStyle.PRIMARY, restoreAction.style)
        assertTrue(callbacks.any { action -> action.data == DailyCallbackData.REFRESH })
        assertTrue(callbacks.any { action -> action.data == TelegramMenuScreen.BACK_CALLBACK_DATA })
        assertEquals(listOf(1, 1, 1, 1), message.actionRows.map(List<*>::size))
    }

    @Test
    fun `claimed daily hides claim action and renders received feedback`() {
        val state = recoverableState().copy(claimedToday = true, recoverable = false)
        val data =
            TelegramMenuScreenData.Daily(
                DailyMenuScreenData(
                    result = DailyRewardStateResult.Available(state),
                    feedback =
                        DailyMenuFeedback(
                            type = DailyMenuFeedbackType.CLAIMED,
                            coins = 81,
                            primes = 1,
                            streak = 8,
                        ),
                ),
            )

        val message = DailyMenuMessageRenderer.render(data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

        assertTrue(message.text.contains("Получено: <b>81 COIN + 1 PRIME</b>"))
        assertTrue(message.text.contains("Серия: <b>8</b>"))
        assertTrue(callbacks.none { action -> action.data == DailyCallbackData.CLAIM })
        assertTrue(callbacks.none { action -> action.data == DailyCallbackData.RESTORE })
    }

    @Test
    fun `unavailable daily keeps refresh and back actions`() {
        val data =
            TelegramMenuScreenData.Daily(
                DailyMenuScreenData(
                    result = DailyRewardStateResult.Unavailable,
                    feedback = null,
                ),
            )

        val message = DailyMenuMessageRenderer.render(data)
        val callbacks = message.actions.filterIsInstance<OutboundTelegramMessageAction.Callback>()

        assertTrue(message.text.contains("Не удалось получить состояние Daily"))
        assertTrue(callbacks.any { action -> action.data == DailyCallbackData.REFRESH })
        assertTrue(callbacks.any { action -> action.data == TelegramMenuScreen.BACK_CALLBACK_DATA })
        assertEquals(listOf(1, 1), message.actionRows.map(List<*>::size))
    }

    private fun recoverableState(): DailyRewardState =
        DailyRewardState(
            accountId = UUID.fromString("01a02e75-2603-7a67-b5f1-98b432273b71"),
            currentStreak = 7,
            claimedToday = false,
            recoverable = true,
            recoverableStreak = 12,
            restoresRemaining = 1,
            nextCoinMin = 70,
            nextCoinMax = 90,
            primeChance = 0.002,
            rewardDate = LocalDate.of(2026, 8, 30),
        )
}
