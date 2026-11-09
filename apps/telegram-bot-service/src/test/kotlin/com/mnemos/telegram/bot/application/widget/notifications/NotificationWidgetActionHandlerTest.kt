package com.mnemos.telegram.bot.application.widget.notifications

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.notification.port.out.NotificationSubscriptionGateway
import com.mnemos.telegram.bot.application.widget.notifications.port.out.NotificationWidgetPublisher
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

class NotificationWidgetActionHandlerTest {
    private val snapshots = mock<NotificationWidgetSnapshotQuery>()
    private val subscriptions = mock<NotificationSubscriptionGateway>()
    private val publisher = mock<NotificationWidgetPublisher>()
    private val sessions = mock<TelegramWidgetSessionRepository>()
    private val responder = mock<WidgetQueryResponder>()
    private val handler = NotificationWidgetActionHandler(snapshots, subscriptions, publisher, sessions, responder)
    private val account = UUID.randomUUID()
    private val drop = UUID.randomUUID()

    @Test
    fun togglesPingThroughExistingGateway() {
        whenever(snapshots.load(11, 0)).thenReturn(
            NotificationWidgetSnapshotResult.Available(view(true)),
            NotificationWidgetSnapshotResult.Available(view(false)),
        )
        whenever(subscriptions.setPingEnabled(11, false)).thenReturn(true)
        whenever(publisher.state(any())).thenReturn("""{"ping":false}""")
        whenever(responder.reply(eq("q"), eq("OK"), any(), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:ping:off"), session()))
        verify(subscriptions).setPingEnabled(11, false)
        verify(publisher).update(eq(11), eq(8), any())
    }

    @Test
    fun refusesDeletionOfSubscriptionNotInVisibleList() {
        whenever(snapshots.load(11, 0)).thenReturn(NotificationWidgetSnapshotResult.Available(view(true)))
        whenever(responder.reply(eq("q"), eq("EXPIRED"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(
            TelegramDeliveryResult.Delivered,
            handler.handle(query("w1:del:" + UUID.randomUUID().toString()), session()),
        )
        verifyNoInteractions(subscriptions)
    }

    private fun query(data: String) = TelegramWidgetQueryContext("q", 11, 11, 8, data, "data", true, false)

    private fun session() = TelegramWidgetSession(11, 8, TelegramWidgetScreen.NOTIFICATIONS, account)

    private fun view(ping: Boolean) =
        NotificationWidgetView(account, 0, 1, 1, ping, listOf(NotificationWidgetDrop(drop, "Міфік")), 1, 2, 3, true)
}
