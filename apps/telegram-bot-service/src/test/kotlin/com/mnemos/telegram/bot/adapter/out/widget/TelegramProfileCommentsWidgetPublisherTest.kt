package com.mnemos.telegram.bot.adapter.out.widget

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.model.TelegramWidgetSendResult
import com.mnemos.telegram.bot.application.widget.port.out.TelegramWidgetGateway
import com.mnemos.telegram.bot.application.widget.profilecomments.ProfileCommentsWidgetView
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import com.mnemos.telegram.bot.presentation.widget.ProfileCommentsWidgetRenderer
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

class TelegramProfileCommentsWidgetPublisherTest {
    private val gateway = mock<TelegramWidgetGateway>()
    private val sessions = mock<TelegramWidgetSessionRepository>()
    private val publisher = TelegramProfileCommentsWidgetPublisher(
        ProfileCommentsWidgetRenderer(ObjectMapper()), gateway, sessions,
    )
    private val profileId = UUID.randomUUID()

    @Test
    fun sendSuccessRegistersOnlyOwnerScopedCommentWidgetSession() {
        whenever(gateway.sendTracked(eq(11L), any())).thenReturn(TelegramWidgetSendResult.Sent(20))

        assertEquals(TelegramDeliveryResult.Delivered, publisher.publish(11L, view()))

        val capture = argumentCaptor<TelegramWidgetSession>()
        verify(sessions).save(capture.capture())
        assertEquals(11L, capture.firstValue.telegramId)
        assertEquals(20, capture.firstValue.messageId)
        assertEquals(TelegramWidgetScreen.PROFILE_COMMENTS, capture.firstValue.screen)
        assertEquals(profileId, capture.firstValue.selectedProfileId)
        assertEquals(null, capture.firstValue.accountId)
    }

    @Test
    fun networkFailureCannotCreateAnEditableWidgetSession() {
        whenever(gateway.sendTracked(eq(11L), any())).thenReturn(TelegramWidgetSendResult.Retryable)

        assertEquals(TelegramDeliveryResult.Retryable, publisher.publish(11L, view()))
        verifyNoInteractions(sessions)
    }

    @Test
    fun existingMessageIsUpdatedWithoutSendingAnotherOne() {
        whenever(gateway.updateChatData(eq(11L), eq(20), any()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, publisher.update(11L, 20, view()))
        verify(gateway).updateChatData(eq(11L), eq(20), any())
        verifyNoInteractions(sessions)
    }

    private fun view() = ProfileCommentsWidgetView(
        profileId, "Kirito", 0, 1, 0L, null, emptyList(),
    )
}
