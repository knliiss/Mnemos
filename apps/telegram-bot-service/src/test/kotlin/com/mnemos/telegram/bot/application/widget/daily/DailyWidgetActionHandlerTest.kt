package com.mnemos.telegram.bot.application.widget.daily

import com.mnemos.telegram.bot.application.daily.model.DailyClaimResult
import com.mnemos.telegram.bot.application.daily.model.DailyClaimedReward
import com.mnemos.telegram.bot.application.daily.model.DailyRestoreResult
import com.mnemos.telegram.bot.application.daily.model.DailyRestoredStreak
import com.mnemos.telegram.bot.application.daily.port.out.DailyRewardGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.daily.port.out.DailyWidgetPublisher
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

class DailyWidgetActionHandlerTest {
    private val snapshots = mock<DailyWidgetSnapshotQuery>()
    private val rewards = mock<DailyRewardGateway>()
    private val publisher = mock<DailyWidgetPublisher>()
    private val responder = mock<WidgetQueryResponder>()
    private val handler = DailyWidgetActionHandler(snapshots, rewards, publisher, responder)
    private val id = UUID.randomUUID()

    @Test
    fun preventsStaleAccountFromClaimingReward() {
        whenever(snapshots.load(11L)).thenReturn(DailyWidgetSnapshotResult.Available(view(UUID.randomUUID())))
        whenever(responder.reply(eq("q"), eq("CONTEXT_CHANGED"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:daily:claim"), session()))
        verifyNoInteractions(rewards)
    }

    @Test
    fun repeatedClaimDoesNotCallRewardGateway() {
        whenever(snapshots.load(11L)).thenReturn(
            DailyWidgetSnapshotResult.Available(view(id, claimed = true)),
        )
        whenever(publisher.state(any())).thenReturn("""{"claimed":true}""")
        whenever(responder.reply(eq("q"), eq("OK"), any(), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:daily:claim"), session()))
        verifyNoInteractions(rewards)
    }

    @Test
    fun restoreUsesExistingDomainGatewayAndReturnsUpdatedState() {
        whenever(snapshots.load(11L)).thenReturn(
            DailyWidgetSnapshotResult.Available(view(id)),
            DailyWidgetSnapshotResult.Available(view(id).copy(recoverable = false)),
        )
        whenever(rewards.restore(11L)).thenReturn(DailyRestoreResult.Restored(DailyRestoredStreak(9)))
        whenever(publisher.state(any())).thenReturn("""{"streak":9}""")
        whenever(responder.reply(eq("q"), eq("OK"), any(), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:daily:restore"), session()))
        verify(rewards).restore(11L)
        verify(publisher).update(eq(11L), eq(8), any())
    }

    @Test
    fun successfulClaimReturnsUpdatedStatus() {
        whenever(snapshots.load(11L)).thenReturn(
            DailyWidgetSnapshotResult.Available(view(id)),
            DailyWidgetSnapshotResult.Available(view(id, claimed = true)),
        )
        whenever(rewards.claim(11L)).thenReturn(DailyClaimResult.Claimed(DailyClaimedReward(7, 50, 2)))
        whenever(publisher.state(any())).thenReturn("""{"claimed":true}""")
        whenever(responder.reply(eq("q"), eq("OK"), any(), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:daily:claim"), session()))
        verify(rewards).claim(11L)
    }

    private fun query(action: String) =
        TelegramWidgetQueryContext("q", 11L, 11L, 8, action, "data", true, false)

    private fun session() =
        TelegramWidgetSession(11L, 8, TelegramWidgetScreen.DAILY, id)

    private fun view(accountId: UUID, claimed: Boolean = false) =
        DailyWidgetView(
            accountId = accountId,
            streak = 7,
            claimedToday = claimed,
            recoverable = true,
            recoverableStreak = 6,
            restoresRemaining = 2,
            nextCoinMin = 20,
            nextCoinMax = 80,
            primeChance = 0.1,
            rewardDate = "2026-11-09",
        )
}
