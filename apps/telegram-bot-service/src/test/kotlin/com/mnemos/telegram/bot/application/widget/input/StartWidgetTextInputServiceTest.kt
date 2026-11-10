package com.mnemos.telegram.bot.application.widget.input

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.message.model.TelegramMessageSendResult
import com.mnemos.telegram.bot.application.widget.input.port.out.WidgetTextInputSessionRepository
import com.mnemos.telegram.bot.application.widget.input.port.out.WidgetTextPromptGateway
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

class StartWidgetTextInputServiceTest {
    private val prompts = mock<WidgetTextPromptGateway>()
    private val sessions = mock<WidgetTextInputSessionRepository>()
    private val service = StartWidgetTextInputService(TelegramWidgetProperties(enabled = true), prompts, sessions)
    private val widget = TelegramWidgetSession(11L, 8, TelegramWidgetScreen.EVENTS, null)

    @Test
    fun editingRoleCreatesPromptBoundToExactRoleId() {
        val roleId = UUID.randomUUID()
        val management = TelegramWidgetSession(11L, 8, TelegramWidgetScreen.ACCOUNT_MANAGEMENT, UUID.randomUUID())
        whenever(prompts.prompt(11L, 8, WidgetTextInputPurpose.ACCOUNT_ROLE_EDIT))
            .thenReturn(TelegramMessageSendResult.Delivered(41))

        assertEquals(TelegramDeliveryResult.Delivered, service.startForRole(management, roleId))
        verify(sessions).save(WidgetTextInputSession(
            11L, 41, 8, management.accountId, WidgetTextInputPurpose.ACCOUNT_ROLE_EDIT, roleId,
        ))
    }

    @Test
    fun roleEditCannotStartWithoutSelectedRole() {
        val management = TelegramWidgetSession(11L, 8, TelegramWidgetScreen.ACCOUNT_MANAGEMENT, UUID.randomUUID())
        assertEquals(
            TelegramDeliveryResult.PermanentFailure("widget-input-unavailable"),
            service.start(management, WidgetTextInputPurpose.ACCOUNT_ROLE_EDIT),
        )
        verifyNoInteractions(prompts, sessions)
    }

    @Test
    fun deliveredPromptCreatesAnIsolatedReplySession() {
        whenever(prompts.prompt(11L, 8, WidgetTextInputPurpose.EVENT_SEARCH))
            .thenReturn(TelegramMessageSendResult.Delivered(41))

        assertEquals(TelegramDeliveryResult.Delivered, service.start(widget, WidgetTextInputPurpose.EVENT_SEARCH))
        verify(sessions).save(WidgetTextInputSession(11L, 41, 8, null, WidgetTextInputPurpose.EVENT_SEARCH))
    }

    @Test
    fun transportFailureNeverCreatesPendingInput() {
        whenever(prompts.prompt(11L, 8, WidgetTextInputPurpose.EVENT_SEARCH))
            .thenReturn(TelegramMessageSendResult.Retryable)

        assertEquals(TelegramDeliveryResult.Retryable, service.start(widget, WidgetTextInputPurpose.EVENT_SEARCH))
        verifyNoInteractions(sessions)
    }

    @Test
    fun unexpectedPromptFailureReturnsRetryableWithoutCrashingPolling() {
        whenever(prompts.prompt(11L, 8, WidgetTextInputPurpose.EVENT_SEARCH))
            .thenThrow(IllegalStateException("transport unavailable"))

        assertEquals(TelegramDeliveryResult.Retryable, service.start(widget, WidgetTextInputPurpose.EVENT_SEARCH))
        verifyNoInteractions(sessions)
    }

    @Test
    fun mismatchedPurposeIsRejectedBeforeNetworkRequest() {
        assertEquals(
            TelegramDeliveryResult.PermanentFailure("widget-input-unavailable"),
            service.start(widget, WidgetTextInputPurpose.ONLINE_PLAYER_SEARCH),
        )
        verifyNoInteractions(prompts, sessions)
    }
}
