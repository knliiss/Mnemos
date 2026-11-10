package com.mnemos.telegram.bot.adapter.out.widget

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.management.AccountManagementWidgetView
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetSendResult
import com.mnemos.telegram.bot.application.widget.port.out.TelegramWidgetGateway
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import com.mnemos.telegram.bot.presentation.widget.AccountManagementWidgetRenderer
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

class TelegramAccountManagementWidgetPublisherTest {
    private val gateway = mock<TelegramWidgetGateway>()
    private val sessions = mock<TelegramWidgetSessionRepository>()
    private val publisher = TelegramAccountManagementWidgetPublisher(AccountManagementWidgetRenderer(ObjectMapper()), gateway, sessions)
    private val accountId = UUID.randomUUID()

    @Test
    fun savesMessageScopedSessionOnlyAfterTelegramSendSucceeds() {
        whenever(gateway.sendTracked(eq(11L), any())).thenReturn(TelegramWidgetSendResult.Sent(20))

        assertEquals(TelegramDeliveryResult.Delivered, publisher.publish(11L, view()))

        val session = argumentCaptor<TelegramWidgetSession>()
        verify(sessions).save(session.capture())
        assertEquals(20, session.firstValue.messageId)
        assertEquals(TelegramWidgetScreen.ACCOUNT_MANAGEMENT, session.firstValue.screen)
        assertEquals(accountId, session.firstValue.accountId)
        assertEquals(2, session.firstValue.page)
    }

    @Test
    fun failedSendNeverPublishesWritableSession() {
        whenever(gateway.sendTracked(eq(11L), any())).thenReturn(TelegramWidgetSendResult.Retryable)
        assertEquals(TelegramDeliveryResult.Retryable, publisher.publish(11L, view()))
        verifyNoInteractions(sessions)
    }

    @Test
    fun stateUpdatesEditExistingWidgetInsteadOfSendingNewMessage() {
        whenever(gateway.updateChatData(eq(11L), eq(20), any())).thenReturn(TelegramDeliveryResult.Delivered)
        assertEquals(TelegramDeliveryResult.Delivered, publisher.update(11L, 20, view()))
        verify(gateway).updateChatData(eq(11L), eq(20), any())
        verifyNoInteractions(sessions)
    }

    private fun view() = AccountManagementWidgetView(
        accountId = accountId,
        nickname = "Kirito",
        page = 2,
        pages = 3,
        totalMembers = 0,
        totalRoles = 0,
        members = emptyList(),
        roles = emptyList(),
        canRemoveMembers = false,
        canAssignRoles = false,
    )
}
