package com.mnemos.telegram.bot.adapter.out.widget

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.dashboard.DashboardWidgetView
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetMessage
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetSendResult
import com.mnemos.telegram.bot.application.widget.port.out.TelegramWidgetGateway
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import com.mnemos.telegram.bot.presentation.widget.DashboardWidgetRenderer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import tools.jackson.databind.ObjectMapper
import java.util.UUID

class TelegramDashboardWidgetPublisherTest {
    private val gateway = mock<TelegramWidgetGateway>()
    private val sessions = mock<TelegramWidgetSessionRepository>()
    private val publisher = TelegramDashboardWidgetPublisher(DashboardWidgetRenderer(ObjectMapper()), gateway, sessions)

    @Test
    fun storesSeparateWidgetSessionWhenTelegramReturnsMessageId() {
        val accountId = UUID.randomUUID()
        val view = view(accountId)
        whenever(gateway.sendTracked(eq(42L), any())).thenReturn(TelegramWidgetSendResult.Sent(99))

        assertEquals(TelegramDeliveryResult.Delivered, publisher.publish(42L, view))

        val captured = argumentCaptor<TelegramWidgetSession>()
        verify(sessions).save(captured.capture())
        assertEquals(99, captured.firstValue.messageId)
        assertEquals(accountId, captured.firstValue.accountId)
        assertEquals(TelegramWidgetScreen.DASHBOARD, captured.firstValue.screen)
    }

    @Test
    fun doesNotRegisterSessionOnFailedTelegramSend() {
        whenever(gateway.sendTracked(eq(42L), any())).thenReturn(TelegramWidgetSendResult.Retryable)
        assertEquals(TelegramDeliveryResult.Retryable, publisher.publish(42L, view(null)))
        verifyNoInteractions(sessions)
    }

    @Test
    fun updateEditsWidgetDataInsteadOfSendingNewMessage() {
        whenever(gateway.updateChatData(eq(42L), eq(99), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, publisher.update(42L, 99, view(null)))

        val content = argumentCaptor<String>()
        verify(gateway).updateChatData(eq(42L), eq(99), content.capture())
        assertEquals(50, ObjectMapper().readTree(content.firstValue).path("coin").asInt())
    }

    private fun view(accountId: UUID?): DashboardWidgetView =
        DashboardWidgetView(
            accountName = "Kirito",
            accountCount = 1,
            coin = 50,
            prime = 7,
            dailyStreak = 4,
            dailyClaimed = false,
            accountId = accountId,
        )
}
