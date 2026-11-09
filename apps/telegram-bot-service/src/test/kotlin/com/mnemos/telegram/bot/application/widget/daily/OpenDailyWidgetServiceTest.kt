package com.mnemos.telegram.bot.application.widget.daily

import com.mnemos.telegram.bot.application.daily.model.DailyRewardState
import com.mnemos.telegram.bot.application.daily.model.DailyRewardStateResult
import com.mnemos.telegram.bot.application.daily.port.out.DailyRewardGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.daily.port.out.DailyWidgetPublisher
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.LocalDate
import java.util.UUID

class OpenDailyWidgetServiceTest {
    private val rewards = mock<DailyRewardGateway>()
    private val publisher = mock<DailyWidgetPublisher>()

    @Test
    fun disabledWidgetNeverLoadsRewards() {
        val service = createService(enabled = false)
        assertEquals(
            TelegramDeliveryResult.PermanentFailure("widgets-disabled"),
            service.execute(11L),
        )
        verifyNoInteractions(rewards, publisher)
    }

    @Test
    fun usersWithoutAccountsSeeAccountGuidance() {
        whenever(rewards.getState(11L)).thenReturn(DailyRewardStateResult.AccountRequired)
        whenever(publisher.publish(eq(11L), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, createService().execute(11L))

        val captured = argumentCaptor<DailyWidgetView>()
        verify(publisher).publish(eq(11L), captured.capture())
        assertEquals(null, captured.firstValue.accountId)
        assertEquals(null, captured.firstValue.streak)
        assertEquals(false, captured.firstValue.recoverable)
    }

    @Test
    fun availableRewardsMapToTheDailyWidget() {
        val id = UUID.randomUUID()
        whenever(rewards.getState(11L)).thenReturn(
            DailyRewardStateResult.Available(
                DailyRewardState(
                    accountId = id,
                    currentStreak = 9,
                    claimedToday = false,
                    recoverable = true,
                    recoverableStreak = 8,
                    restoresRemaining = 2,
                    nextCoinMin = 20,
                    nextCoinMax = 80,
                    primeChance = 0.15,
                    rewardDate = LocalDate.of(2026, 11, 9),
                ),
            ),
        )
        whenever(publisher.publish(eq(11L), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, createService().execute(11L))

        val captured = argumentCaptor<DailyWidgetView>()
        verify(publisher).publish(eq(11L), captured.capture())
        assertEquals(id, captured.firstValue.accountId)
        assertEquals(9, captured.firstValue.streak)
        assertEquals(true, captured.firstValue.recoverable)
        assertEquals(2, captured.firstValue.restoresRemaining)
        assertEquals("2026-11-09", captured.firstValue.rewardDate)
    }

    private fun createService(enabled: Boolean = true) =
        OpenDailyWidgetService(TelegramWidgetProperties(enabled = enabled), rewards, publisher)
}
