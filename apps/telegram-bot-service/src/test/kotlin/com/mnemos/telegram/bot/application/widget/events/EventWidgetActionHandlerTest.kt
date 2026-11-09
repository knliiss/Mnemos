package com.mnemos.telegram.bot.application.widget.events

import com.mnemos.telegram.bot.application.events.model.EventHistoryFilter
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.events.port.out.EventWidgetPublisher
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

class EventWidgetActionHandlerTest {
    private val snapshots = mock<EventWidgetSnapshotQuery>()
    private val opener = mock<OpenEventWidgetUseCase>()
    private val publisher = mock<EventWidgetPublisher>()
    private val sessions = mock<TelegramWidgetSessionRepository>()
    private val responder = mock<WidgetQueryResponder>()
    private val handler = EventWidgetActionHandler(snapshots, opener, publisher, sessions, responder)

    @Test
    fun refusesCursorNotInCurrentPage() {
        val current = UUID.randomUUID()
        whenever(snapshots.load(11L, EventHistoryFilter.ALL, null))
            .thenReturn(EventWidgetSnapshotResult.Available(view(current)))
        whenever(responder.reply(eq("q"), eq("CONFLICT"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        val result = handler.handle(query("w1:evt:next:" + UUID.randomUUID().toString()), session())

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verifyNoInteractions(opener, publisher)
    }

    @Test
    fun filterChangeUsesAQueryAndSendsStateUpdate() {
        whenever(snapshots.load(11L, EventHistoryFilter.ALL, null))
            .thenReturn(EventWidgetSnapshotResult.Available(view(null)))
        whenever(snapshots.load(11L, EventHistoryFilter.ITEM_DROP, null))
            .thenReturn(EventWidgetSnapshotResult.Available(view(null).copy(filter = EventHistoryFilter.ITEM_DROP)))
        whenever(publisher.state(any())).thenReturn("""{"filter":"ITEM_DROP","items":[]}""")
        whenever(responder.reply(eq("q"), eq("OK"), any(), any()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        val result = handler.handle(query("w1:evt:filter:ITEM_DROP"), session())

        assertEquals(TelegramDeliveryResult.Delivered, result)
        verify(sessions).save(session().copy(page = EventHistoryFilter.ITEM_DROP.ordinal))
        verify(publisher).update(eq(11L), eq(8), any())
    }

    @Test
    fun validNextCursorOpensNewMessageInsteadOfChangingOldPage() {
        val current = UUID.randomUUID()
        whenever(snapshots.load(11L, EventHistoryFilter.ALL, null))
            .thenReturn(EventWidgetSnapshotResult.Available(view(current)))
        whenever(opener.execute(11L, EventHistoryFilter.ALL, current))
            .thenReturn(TelegramDeliveryResult.Delivered)
        whenever(responder.reply(eq("q"), eq("OK"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:evt:next:$current"), session()))
        verify(opener).execute(11L, EventHistoryFilter.ALL, current)
    }

    private fun query(data: String) =
        TelegramWidgetQueryContext("q", 11L, 11L, 8, data, "data", true, false)

    private fun session() =
        TelegramWidgetSession(11L, 8, TelegramWidgetScreen.EVENTS, null, EventHistoryFilter.ALL.ordinal)

    private fun view(cursor: UUID?) =
        EventWidgetView(EventHistoryFilter.ALL, null, cursor, emptyList())
}
