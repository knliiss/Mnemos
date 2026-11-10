package com.mnemos.telegram.bot.application.widget.profilecomments

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.message.model.TelegramMessageSendResult
import com.mnemos.telegram.bot.application.profilecomments.port.out.ProfileCommentActorAccountGateway
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputPurpose
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputSession
import com.mnemos.telegram.bot.application.widget.input.port.out.WidgetTextInputSessionRepository
import com.mnemos.telegram.bot.application.widget.input.port.out.WidgetTextPromptGateway
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

class StartProfileCommentInputServiceTest {
    private val actors = mock<ProfileCommentActorAccountGateway>()
    private val prompts = mock<WidgetTextPromptGateway>()
    private val sessions = mock<WidgetTextInputSessionRepository>()
    private val service = StartProfileCommentInputService(
        TelegramWidgetProperties(enabled = true), actors, prompts, sessions,
    )
    private val profileId = UUID.randomUUID()
    private val actorId = UUID.randomUUID()
    private val widget = TelegramWidgetSession(
        11L, 8, TelegramWidgetScreen.PROFILE_COMMENTS, null, selectedProfileId = profileId,
    )

    @Test
    fun rootCommentStoresExpectedAuthorTargetAndUniqueOperation() {
        whenever(actors.activeAccountId(11L)).thenReturn(actorId)
        whenever(prompts.prompt(11L, 8, WidgetTextInputPurpose.PROFILE_COMMENT_CREATE))
            .thenReturn(TelegramMessageSendResult.Delivered(41))

        assertEquals(TelegramDeliveryResult.Delivered, service.execute(widget))
        val session = argumentCaptor<WidgetTextInputSession>()
        verify(sessions).save(session.capture())
        assertEquals(profileId, session.firstValue.targetProfileId)
        assertEquals(actorId, session.firstValue.expectedActorAccountId)
        assertEquals(null, session.firstValue.targetCommentId)
        assertEquals(WidgetTextInputPurpose.PROFILE_COMMENT_CREATE, session.firstValue.purpose)
        org.junit.jupiter.api.Assertions.assertNotNull(session.firstValue.mutationId)
    }

    @Test
    fun replyIncludesRootIdAndReplyPurpose() {
        val rootId = UUID.randomUUID()
        whenever(actors.activeAccountId(11L)).thenReturn(actorId)
        whenever(prompts.prompt(11L, 8, WidgetTextInputPurpose.PROFILE_COMMENT_REPLY))
            .thenReturn(TelegramMessageSendResult.Delivered(42))

        assertEquals(TelegramDeliveryResult.Delivered, service.execute(widget.copy(cursor = rootId)))
        val session = argumentCaptor<WidgetTextInputSession>()
        verify(sessions).save(session.capture())
        assertEquals(rootId, session.firstValue.targetCommentId)
        assertEquals(WidgetTextInputPurpose.PROFILE_COMMENT_REPLY, session.firstValue.purpose)
    }

    @Test
    fun inactiveAccountNeverCreatesInputPrompt() {
        assertEquals(
            TelegramDeliveryResult.PermanentFailure("active-account-required"),
            service.execute(widget),
        )
        verifyNoInteractions(prompts, sessions)
    }

    @Test
    fun failedPromptNeverPersistsAnActionableInput() {
        whenever(actors.activeAccountId(11L)).thenReturn(actorId)
        whenever(prompts.prompt(11L, 8, WidgetTextInputPurpose.PROFILE_COMMENT_CREATE))
            .thenReturn(TelegramMessageSendResult.Retryable)
        assertEquals(TelegramDeliveryResult.Retryable, service.execute(widget))
        verifyNoInteractions(sessions)
    }
}
