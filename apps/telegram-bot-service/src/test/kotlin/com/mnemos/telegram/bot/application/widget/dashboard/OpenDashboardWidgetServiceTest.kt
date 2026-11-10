package com.mnemos.telegram.bot.application.widget.dashboard

import com.mnemos.telegram.bot.application.account.model.TelegramAccountBalance
import com.mnemos.telegram.bot.application.account.model.TelegramAccountBalanceResult
import com.mnemos.telegram.bot.application.account.model.TelegramAccountContext
import com.mnemos.telegram.bot.application.account.model.TelegramAccountContextResult
import com.mnemos.telegram.bot.application.account.model.TelegramAccountStatus
import com.mnemos.telegram.bot.application.account.model.TelegramAccountSummary
import com.mnemos.telegram.bot.application.account.port.out.AccountBalanceGateway
import com.mnemos.telegram.bot.application.account.port.out.AccountGateway
import com.mnemos.telegram.bot.application.daily.model.DailyRewardState
import com.mnemos.telegram.bot.application.daily.model.DailyRewardStateResult
import com.mnemos.telegram.bot.application.daily.port.out.DailyRewardGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.dashboard.port.out.DashboardWidgetPublisher
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

class OpenDashboardWidgetServiceTest {
    private val accounts = mock<AccountGateway>()
    private val balances = mock<AccountBalanceGateway>()
    private val rewards = mock<DailyRewardGateway>()
    private val publisher = mock<DashboardWidgetPublisher>()

    @Test
    fun disabledWidgetsNeverCallExternalServices() {
        val service = createService(enabled = false)

        val result = service.execute(42L)

        assertEquals(TelegramDeliveryResult.PermanentFailure("widgets-disabled"), result)
        verifyNoInteractions(accounts, balances, rewards, publisher)
    }

    @Test
    fun registrationRequiredStillRendersHelpfulDashboard() {
        whenever(accounts.getContext(42L)).thenReturn(TelegramAccountContextResult.RegistrationRequired)
        whenever(publisher.publish(eq(42L), any())).thenReturn(TelegramDeliveryResult.Delivered)

        val result = createService().execute(42L)

        assertEquals(TelegramDeliveryResult.Delivered, result)
        val view = argumentCaptor<DashboardWidgetView>()
        verify(publisher).publish(eq(42L), view.capture())
        assertEquals(null, view.firstValue.accountName)
        verifyNoInteractions(balances, rewards)
    }

    @Test
    fun activeAccountSnapshotReusesExistingGateways() {
        val accountId = UUID.randomUUID()
        val context =
            TelegramAccountContext(
                activeAccountId = accountId,
                accounts =
                    listOf(
                        TelegramAccountSummary(
                            accountId = accountId,
                            cristalixUuid = UUID.randomUUID(),
                            cristalixNickname = "Kirito",
                            memberCount = 2,
                            maxMembers = 5,
                            owner = true,
                            active = true,
                            status = TelegramAccountStatus.ACTIVE,
                        ),
                    ),
            )
        whenever(accounts.getContext(42L)).thenReturn(TelegramAccountContextResult.Available(context))
        whenever(balances.getBalances(42L)).thenReturn(
            TelegramAccountBalanceResult.Available(TelegramAccountBalance(coin = 250, prime = 12)),
        )
        whenever(rewards.getState(42L)).thenReturn(
            DailyRewardStateResult.Available(
                DailyRewardState(
                    accountId = accountId,
                    currentStreak = 8,
                    claimedToday = true,
                    recoverable = false,
                    recoverableStreak = 0,
                    restoresRemaining = 1,
                    nextCoinMin = 10,
                    nextCoinMax = 30,
                    primeChance = 0.1,
                    rewardDate = LocalDate.of(2026, 11, 9),
                ),
            ),
        )
        whenever(publisher.publish(eq(42L), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, createService().execute(42L))

        val view = argumentCaptor<DashboardWidgetView>()
        verify(publisher).publish(eq(42L), view.capture())
        assertEquals("Kirito", view.firstValue.accountName)
        assertEquals(accountId, view.firstValue.accountId)
        assertEquals(250L, view.firstValue.coin)
        assertEquals(8, view.firstValue.dailyStreak)
        assertEquals(true, view.firstValue.dailyClaimed)
    }

    @Test
    fun unavailableAccountServiceDoesNotSendMisleadingDashboard() {
        whenever(accounts.getContext(42L)).thenReturn(TelegramAccountContextResult.Unavailable)

        assertEquals(TelegramDeliveryResult.Retryable, createService().execute(42L))
        verifyNoInteractions(balances, rewards, publisher)
    }

    private fun createService(enabled: Boolean = true): OpenDashboardWidgetService =
        OpenDashboardWidgetService(
            properties = TelegramWidgetProperties(enabled = enabled),
            accounts = accounts,
            balances = balances,
            rewards = rewards,
            publisher = publisher,
        )
}
