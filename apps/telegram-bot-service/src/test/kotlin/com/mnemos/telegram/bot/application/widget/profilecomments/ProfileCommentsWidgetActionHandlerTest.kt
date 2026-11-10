package com.mnemos.telegram.bot.application.widget.profilecomments

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.profilecomments.port.out.ProfileCommentsWidgetPublisher
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

class ProfileCommentsWidgetActionHandlerTest {
    private val snapshots = mock<ProfileCommentsWidgetSnapshotQuery>()
    private val publisher = mock<ProfileCommentsWidgetPublisher>()
    private val sessions = mock<TelegramWidgetSessionRepository>()
    private val responder = mock<WidgetQueryResponder>()
    private val handler = ProfileCommentsWidgetActionHandler(snapshots, publisher, sessions, responder)
    private val profileId = UUID.randomUUID()
    private val commentId = UUID.randomUUID()
    private val session = TelegramWidgetSession(
        telegramId = 11L,
        messageId = 8,
        screen = TelegramWidgetScreen.PROFILE_COMMENTS,
        accountId = null,
        selectedProfileId = profileId,
    )

    @Test
    fun opensOnlyMatchingRootCommentFromCurrentServerPage() {
        whenever(snapshots.load(11L, profileId, 0, null))
            .thenReturn(ProfileCommentsWidgetSnapshotResult.Available(view()))
        whenever(snapshots.load(11L, profileId, 0, commentId))
            .thenReturn(ProfileCommentsWidgetSnapshotResult.Available(view(root = commentId)))
        whenever(publisher.state(any())).thenReturn("{}")
        whenever(responder.reply(eq("q"), eq("OK"), any(), any()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(
            TelegramDeliveryResult.Delivered,
            handler.handle(query("w1:comments:thread:0:" + tag(commentId)), session),
        )
        verify(sessions).save(session.copy(cursor = commentId, page = 0))
        verify(publisher).update(eq(11L), eq(8), any())
    }

    @Test
    fun staleCommentIndexCannotOpenAnotherAuthorsThread() {
        whenever(snapshots.load(11L, profileId, 0, null))
            .thenReturn(ProfileCommentsWidgetSnapshotResult.Available(view()))
        whenever(responder.reply(eq("q"), eq("CONFLICT"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(
            TelegramDeliveryResult.Delivered,
            handler.handle(query("w1:comments:thread:0:" + tag(UUID.randomUUID())), session),
        )
        verifyNoInteractions(publisher, sessions)
    }

    @Test
    fun paginationCannotUseStalePageValue() {
        whenever(snapshots.load(11L, profileId, 0, null))
            .thenReturn(ProfileCommentsWidgetSnapshotResult.Available(view()))
        whenever(responder.reply(eq("q"), eq("CONFLICT"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:comments:next:6"), session))
        verifyNoInteractions(publisher, sessions)
    }

    @Test
    fun cannotReadCommentsWithoutProfileContext() {
        whenever(responder.reply(eq("q"), eq("INVALID"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(
            TelegramDeliveryResult.Delivered,
            handler.handle(query("w1:comments:refresh"), session.copy(selectedProfileId = null)),
        )
        verifyNoInteractions(snapshots)
    }

    @Test
    fun backFromRepliesRestoresRootCommentPage() {
        val selected = session.copy(page = 1, cursor = commentId)
        whenever(snapshots.load(11L, profileId, 1, commentId))
            .thenReturn(ProfileCommentsWidgetSnapshotResult.Available(view(page = 1, root = commentId)))
        whenever(snapshots.load(11L, profileId, 0, null))
            .thenReturn(ProfileCommentsWidgetSnapshotResult.Available(view()))
        whenever(publisher.state(any())).thenReturn("{}")
        whenever(responder.reply(eq("q"), eq("OK"), any(), any()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:comments:back"), selected))
        verify(sessions).save(selected.copy(page = 0, cursor = null))
    }

    private fun view(page: Int = 0, root: UUID? = null) =
        ProfileCommentsWidgetView(
            profileId = profileId,
            nickname = "Kirito",
            page = page,
            totalPages = 2,
            totalItems = 4,
            rootCommentId = root,
            comments = listOf(
                ProfileCommentWidgetItem(commentId, "Asuna", "test", false, 2, 1, "2026-11-10 15:00"),
            ),
        )

    private fun tag(id: UUID): String = id.toString().replace("-", "").take(12)

    private fun query(action: String) = TelegramWidgetQueryContext("q", 11L, 11L, 8, action, "data", true, false)
}
