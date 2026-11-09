package com.mnemos.telegram.bot.application.widget.query

import com.mnemos.telegram.bot.application.daily.model.DailyClaimResult
import com.mnemos.telegram.bot.application.daily.model.DailyClaimedReward
import com.mnemos.telegram.bot.application.daily.port.out.DailyRewardGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.dashboard.DashboardWidgetSnapshotQuery
import com.mnemos.telegram.bot.application.widget.dashboard.DashboardWidgetSnapshotResult
import com.mnemos.telegram.bot.application.widget.dashboard.DashboardWidgetView
import com.mnemos.telegram.bot.application.widget.dashboard.port.out.DashboardWidgetPublisher
import com.mnemos.telegram.bot.application.widget.port.out.TelegramWidgetGateway
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import tools.jackson.databind.ObjectMapper
import java.util.UUID

class TelegramWidgetQueryRouterTest {
    private val sessions = mock<TelegramWidgetSessionRepository>()
    private val snapshots = mock<DashboardWidgetSnapshotQuery>()
    private val publisher = mock<DashboardWidgetPublisher>()
    private val rewards = mock<DailyRewardGateway>()
    private val gateway = mock<TelegramWidgetGateway>()
    private val router = TelegramWidgetQueryRouter(sessions, snapshots, publisher, rewards, gateway, ObjectMapper())

    @Test
    fun rejectsAnotherActorBeforeLoadingSessionOrClaiming() {
        whenever(gateway.answerQuery(eq("q1"), any())).thenReturn(TelegramDeliveryResult.Delivered)
        val result = router.route(query(actor = 22L))

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verifyNoInteractions(sessions, snapshots, rewards, publisher)
    }

    @Test
    fun deniesStaleAccountWidgetWithoutClaiming() {
        val accountId = UUID.randomUUID()
        whenever(sessions.find(11L, 8)).thenReturn(TelegramWidgetSession(11L, 8, TelegramWidgetScreen.DASHBOARD, accountId))
        whenever(snapshots.load(11L)).thenReturn(DashboardWidgetSnapshotResult.Available(view(UUID.randomUUID())))
        whenever(gateway.answerQuery(eq("q1"), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, router.route(query(data = "w1:claim")))
        verifyNoInteractions(rewards)
    }

    @Test
    fun claimingRewardReturnsFreshStateAndEditsMessageData() {
        val accountId = UUID.randomUUID()
        whenever(sessions.find(11L, 8)).thenReturn(TelegramWidgetSession(11L, 8, TelegramWidgetScreen.DASHBOARD, accountId))
        whenever(snapshots.load(11L)).thenReturn(
            DashboardWidgetSnapshotResult.Available(view(accountId)),
            DashboardWidgetSnapshotResult.Available(view(accountId).copy(dailyClaimed = true)),
        )
        whenever(rewards.claim(11L)).thenReturn(DailyClaimResult.Claimed(DailyClaimedReward(streak = 2, coins = 20, primes = 0)))
        whenever(publisher.state(any())).thenReturn("""{"name":"Kirito","claimed":true}""")
        whenever(gateway.answerQuery(eq("q1"), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, router.route(query(data = "w1:claim")))

        verify(rewards).claim(11L)
        verify(publisher).update(eq(11L), eq(8), any())
    }

    private fun query(
        actor: Long = 11L,
        data: String = "w1:refresh",
    ): TelegramWidgetQueryContext =
        TelegramWidgetQueryContext(
            queryId = "q1",
            chatId = 11L,
            actorId = actor,
            messageId = 8,
            data = data,
            type = "data",
            isPrivate = true,
            isShared = false,
        )

    private fun view(accountId: UUID): DashboardWidgetView =
        DashboardWidgetView(
            accountName = "Kirito",
            accountCount = 1,
            coin = 50,
            prime = 7,
            dailyStreak = 2,
            dailyClaimed = false,
            accountId = accountId,
        )
}
