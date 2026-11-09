package com.mnemos.telegram.bot.application.widget.dashboard

import com.mnemos.telegram.bot.application.daily.model.DailyClaimResult
import com.mnemos.telegram.bot.application.daily.model.DailyClaimedReward
import com.mnemos.telegram.bot.application.daily.port.out.DailyRewardGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.dashboard.port.out.DashboardWidgetPublisher
import com.mnemos.telegram.bot.application.widget.notifications.OpenNotificationWidgetUseCase
import com.mnemos.telegram.bot.application.widget.events.OpenEventWidgetUseCase
import com.mnemos.telegram.bot.application.widget.accounts.OpenAccountWidgetUseCase
import com.mnemos.telegram.bot.application.widget.daily.OpenDailyWidgetUseCase
import com.mnemos.telegram.bot.application.widget.bets.OpenBetWidgetUseCase
import com.mnemos.telegram.bot.application.widget.wallet.OpenWalletWidgetUseCase
import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetQueryContext
import com.mnemos.telegram.bot.application.widget.query.port.out.WidgetQueryResponder
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.isNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

class DashboardWidgetActionHandlerTest {
    private val snapshots = mock<DashboardWidgetSnapshotQuery>()
    private val publisher = mock<DashboardWidgetPublisher>()
    private val rewards = mock<DailyRewardGateway>()
    private val notifications = mock<OpenNotificationWidgetUseCase>()
    private val events = mock<OpenEventWidgetUseCase>()
    private val accounts = mock<OpenAccountWidgetUseCase>()
    private val daily = mock<OpenDailyWidgetUseCase>()
    private val bets = mock<OpenBetWidgetUseCase>()
    private val wallet = mock<OpenWalletWidgetUseCase>()
    private val responder = mock<WidgetQueryResponder>()
    private val handler = DashboardWidgetActionHandler(snapshots, publisher, rewards, notifications, events, accounts, daily, bets, wallet, responder)

    @Test
    fun staleAccountDoesNotClaimRewards() {
        whenever(snapshots.load(11)).thenReturn(DashboardWidgetSnapshotResult.Available(view(UUID.randomUUID())))
        whenever(responder.reply(eq("q"), eq("CONTEXT_CHANGED"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:claim"), session(UUID.randomUUID())))
        verifyNoInteractions(rewards)
    }

    @Test
    fun claimUsesRewardGatewayAndUpdatesDashboard() {
        val id = UUID.randomUUID()
        whenever(snapshots.load(11)).thenReturn(
            DashboardWidgetSnapshotResult.Available(view(id)),
            DashboardWidgetSnapshotResult.Available(view(id).copy(dailyClaimed = true)),
        )
        whenever(rewards.claim(11)).thenReturn(DailyClaimResult.Claimed(DailyClaimedReward(2, 30, 0)))
        whenever(publisher.state(any())).thenReturn("""{"claimed":true}""")
        whenever(responder.reply(eq("q"), eq("OK"), any(), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:claim"), session(id)))
        verify(rewards).claim(11)
        verify(publisher).update(eq(11), eq(8), any())
    }

    private fun query(data: String) = TelegramWidgetQueryContext("q", 11, 11, 8, data, "data", true, false)

    private fun session(id: UUID) = TelegramWidgetSession(11, 8, TelegramWidgetScreen.DASHBOARD, id)

    private fun view(id: UUID) = DashboardWidgetView("Kirito", 1, 50, 7, 2, false, id)
}
