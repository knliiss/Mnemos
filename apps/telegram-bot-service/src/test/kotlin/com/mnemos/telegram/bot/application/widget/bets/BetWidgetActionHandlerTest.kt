package com.mnemos.telegram.bot.application.widget.bets

import com.mnemos.telegram.bot.application.bet.model.BetMenuMode
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.bets.port.out.BetWidgetPublisher
import com.mnemos.telegram.bot.application.widget.query.TelegramWidgetQueryContext
import com.mnemos.telegram.bot.application.widget.query.port.out.WidgetQueryResponder
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
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

class BetWidgetActionHandlerTest {
    private val snapshots = mock<BetWidgetSnapshotQuery>()
    private val publisher = mock<BetWidgetPublisher>()
    private val sessions = mock<TelegramWidgetSessionRepository>()
    private val responder = mock<WidgetQueryResponder>()
    private val handler = BetWidgetActionHandler(snapshots, publisher, sessions, responder)
    private val id = UUID.randomUUID()

    @Test
    fun rejectsOldAccountBeforeChangingMode() {
        whenever(snapshots.load(11L, BetMenuMode.ROUNDS))
            .thenReturn(BetWidgetSnapshotResult.Available(view(UUID.randomUUID(), BetMenuMode.ROUNDS)))
        whenever(responder.reply(eq("q"), eq("CONTEXT_CHANGED"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:bet:mode:HISTORY"), session()))
        verifyNoInteractions(publisher, sessions)
    }

    @Test
    fun switchingModeOnlyLoadsReadOnlySnapshots() {
        whenever(snapshots.load(11L, BetMenuMode.ROUNDS))
            .thenReturn(BetWidgetSnapshotResult.Available(view(id, BetMenuMode.ROUNDS)))
        whenever(snapshots.load(11L, BetMenuMode.RACES))
            .thenReturn(BetWidgetSnapshotResult.Available(view(id, BetMenuMode.RACES)))
        whenever(publisher.state(any())).thenReturn("""{"mode":"RACES"}""")
        whenever(responder.reply(eq("q"), eq("OK"), any(), any()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:bet:mode:RACES"), session()))
        verify(sessions).save(session().copy(page = BetMenuMode.RACES.ordinal))
        verify(publisher).update(eq(11L), eq(8), any())
    }

    private fun query(data: String) =
        TelegramWidgetQueryContext("q", 11L, 11L, 8, data, "data", true, false)

    private fun session() =
        TelegramWidgetSession(11L, 8, TelegramWidgetScreen.BETS, id, BetMenuMode.ROUNDS.ordinal)

    private fun view(accountId: UUID, mode: BetMenuMode) =
        BetWidgetView(accountId, mode, 1, listOf(BetWidgetEntry("Global", "YES 2.0", "OPEN")))
}
