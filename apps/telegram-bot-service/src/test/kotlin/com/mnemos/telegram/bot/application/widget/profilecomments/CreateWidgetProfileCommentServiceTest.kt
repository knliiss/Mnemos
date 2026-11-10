package com.mnemos.telegram.bot.application.widget.profilecomments

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.profilecomments.ProfileCommentCreationResult
import com.mnemos.telegram.bot.application.profilecomments.port.out.ProfileCommentActorAccountGateway
import com.mnemos.telegram.bot.application.profilecomments.port.out.ProfileCommentMutationGateway
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputPurpose
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputSession
import com.mnemos.telegram.bot.application.widget.profilecomments.port.out.ProfileCommentsWidgetPublisher
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetScreen
import com.mnemos.telegram.bot.application.widget.session.TelegramWidgetSession
import com.mnemos.telegram.bot.application.widget.session.port.out.TelegramWidgetSessionRepository
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.util.UUID

class CreateWidgetProfileCommentServiceTest {
    private val authors = mock<ProfileCommentActorAccountGateway>()
    private val mutations = mock<ProfileCommentMutationGateway>()
    private val snapshots = mock<ProfileCommentsWidgetSnapshotQuery>()
    private val publisher = mock<ProfileCommentsWidgetPublisher>()
    private val sessions = mock<TelegramWidgetSessionRepository>()
    private val service = CreateWidgetProfileCommentService(authors, mutations, snapshots, publisher, sessions)
    private val profileId = UUID.randomUUID()
    private val actorAccountId = UUID.randomUUID()
    private val operationId = UUID.randomUUID()
    private val widget = TelegramWidgetSession(11L, 8, TelegramWidgetScreen.PROFILE_COMMENTS, null, selectedProfileId = profileId)
    private val input = WidgetTextInputSession(
        11L, 41, 8, null, WidgetTextInputPurpose.PROFILE_COMMENT_CREATE,
        targetProfileId = profileId, expectedActorAccountId = actorAccountId, mutationId = operationId,
    )

    @Test
    fun createsCommentWithPinnedIdentityAndIdempotencyKey() {
        whenever(authors.activeAccountId(11L)).thenReturn(actorAccountId)
        whenever(mutations.create(11L, profileId, actorAccountId, operationId, null, "Вітаю"))
            .thenReturn(ProfileCommentCreationResult.Created(UUID.randomUUID()))
        whenever(snapshots.load(11L, profileId, 0, null))
            .thenReturn(ProfileCommentsWidgetSnapshotResult.Available(view()))
        whenever(publisher.update(eq(11L), eq(8), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, service.create(input, widget, " Вітаю "))
        verify(mutations).create(11L, profileId, actorAccountId, operationId, null, "Вітаю")
        verify(sessions).save(widget.copy(page = 0))
    }

    @Test
    fun replyRetainsItsExactRootCommentIdentity() {
        val parent = UUID.randomUUID()
        val threadWidget = widget.copy(cursor = parent)
        val threadInput = input.copy(
            purpose = WidgetTextInputPurpose.PROFILE_COMMENT_REPLY,
            targetCommentId = parent,
        )
        whenever(authors.activeAccountId(11L)).thenReturn(actorAccountId)
        whenever(mutations.create(11L, profileId, actorAccountId, operationId, parent, "Reply"))
            .thenReturn(ProfileCommentCreationResult.Created(UUID.randomUUID()))
        whenever(snapshots.load(11L, profileId, 0, parent))
            .thenReturn(ProfileCommentsWidgetSnapshotResult.Available(view(parent)))
        whenever(publisher.update(eq(11L), eq(8), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, service.create(threadInput, threadWidget, "Reply"))
        verify(mutations).create(11L, profileId, actorAccountId, operationId, parent, "Reply")
    }

    @Test
    fun switchedAccountBetweenPromptAndReplyFailsClosed() {
        whenever(authors.activeAccountId(11L)).thenReturn(UUID.randomUUID())

        assertEquals(
            TelegramDeliveryResult.PermanentFailure("comment-author-account-changed"),
            service.create(input, widget, "Вітаю"),
        )
        verifyNoInteractions(mutations, snapshots, publisher)
    }

    @Test
    fun changedWidgetThreadCannotRedirectAReply() {
        val parent = UUID.randomUUID()
        val stale = input.copy(
            purpose = WidgetTextInputPurpose.PROFILE_COMMENT_REPLY, targetCommentId = parent,
        )
        assertEquals(
            TelegramDeliveryResult.PermanentFailure("invalid-comment-input-context"),
            service.create(stale, widget, "Reply"),
        )
        verifyNoInteractions(authors, mutations, publisher)
    }

    @Test
    fun invalidBodyDoesNotReachBackend() {
        whenever(authors.activeAccountId(11L)).thenReturn(actorAccountId)
        whenever(snapshots.load(11L, profileId, 0, null))
            .thenReturn(ProfileCommentsWidgetSnapshotResult.Available(view()))
        whenever(publisher.update(eq(11L), eq(8), any())).thenReturn(TelegramDeliveryResult.Delivered)
        assertEquals(TelegramDeliveryResult.Delivered, service.create(input, widget, " "))
        verifyNoInteractions(mutations)
    }

    @Test
    fun failedWidgetRefreshReturnsRetryableToPermitSafeBackendReplay() {
        whenever(authors.activeAccountId(11L)).thenReturn(actorAccountId)
        whenever(mutations.create(11L, profileId, actorAccountId, operationId, null, "Text"))
            .thenReturn(ProfileCommentCreationResult.Created(UUID.randomUUID()))
        whenever(snapshots.load(11L, profileId, 0, null)).thenReturn(ProfileCommentsWidgetSnapshotResult.Unavailable)

        assertEquals(TelegramDeliveryResult.Retryable, service.create(input, widget, "Text"))
        verifyNoInteractions(sessions)
    }

    private fun view(root: UUID? = null) =
        ProfileCommentsWidgetView(profileId, "Kirito", 0, 1, 0L, root, emptyList())
}
