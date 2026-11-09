package com.mnemos.telegram.bot.adapter.out.widget

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.dashboard.DashboardWidgetView
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetMessage
import com.mnemos.telegram.bot.application.widget.port.out.TelegramWidgetGateway
import com.mnemos.telegram.bot.presentation.widget.DashboardWidgetRenderer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class TelegramDashboardWidgetPublisherTest {
    @Test
    fun translatesDashboardSnapshotIntoWidgetAndSendsThroughTransport() {
        val gateway = mock<TelegramWidgetGateway>()
        whenever(gateway.send(eq(11L), any())).thenReturn(TelegramDeliveryResult.Delivered)
        val publisher = TelegramDashboardWidgetPublisher(DashboardWidgetRenderer(), gateway)

        val result =
            publisher.publish(
                11L,
                DashboardWidgetView(
                    accountName = "Asuna",
                    accountCount = 2,
                    coin = 200,
                    prime = 10,
                    dailyStreak = 4,
                    dailyClaimed = true,
                ),
            )

        assertEquals(TelegramDeliveryResult.Delivered, result)
        val widget = argumentCaptor<TelegramWidgetMessage>()
        verify(gateway).send(eq(11L), widget.capture())
        assertTrue(widget.firstValue.html.contains("Asuna"))
        assertTrue(widget.firstValue.html.contains(">200<"))
        assertTrue(widget.firstValue.html.contains("Отримано сьогодні"))
    }
}
