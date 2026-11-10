package com.mnemos.telegram.bot.application.widget.profileactivity

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.profileactivity.port.out.ProfileActivityWidgetPublisher
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

class ProfileActivityWidgetActionHandlerTest {
    private val snapshots = mock<ProfileActivitySnapshotQuery>()
    private val publisher = mock<ProfileActivityWidgetPublisher>()
    private val sessions = mock<TelegramWidgetSessionRepository>()
    private val responder = mock<WidgetQueryResponder>()
    private val handler = ProfileActivityWidgetActionHandler(snapshots, publisher, sessions, responder)
    private val profileId = UUID.randomUUID()
    private val next = UUID.randomUUID()
    private val session = TelegramWidgetSession(
        telegramId = 11L,
        messageId = 8,
        screen = TelegramWidgetScreen.PROFILE_ACTIVITY,
        accountId = null,
        selectedProfileId = profileId,
    )

    @Test
    fun nextPageRequiresCurrentCursorFromServer() {
        whenever(snapshots.load(11L, profileId, null))
            .thenReturn(ProfileActivitySnapshotResult.Available(view(nextCursor = next)))
        whenever(snapshots.load(11L, profileId, next))
            .thenReturn(ProfileActivitySnapshotResult.Available(view(after = next)))
        whenever(publisher.state(any())).thenReturn("{}")
        whenever(responder.reply(eq("q"), eq("OK"), any(), any()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(
            TelegramDeliveryResult.Delivered,
            handler.handle(query("w1:activity:next:$next"), session),
        )
        verify(sessions).save(session.copy(cursor = next))
        verify(publisher).update(eq(11L), eq(8), any())
    }

    @Test
    fun forgedNextCursorCannotChangeVisiblePage() {
        whenever(snapshots.load(11L, profileId, null))
            .thenReturn(ProfileActivitySnapshotResult.Available(view(nextCursor = next)))
        whenever(responder.reply(eq("q"), eq("CONFLICT"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(
            TelegramDeliveryResult.Delivered,
            handler.handle(query("w1:activity:next:" + UUID.randomUUID()), session),
        )
        verifyNoInteractions(publisher, sessions)
    }

    @Test
    fun absentProfileSelectionCannotReadActivity() {
        whenever(responder.reply(eq("q"), eq("INVALID"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(
            TelegramDeliveryResult.Delivered,
            handler.handle(query("w1:activity:refresh"), session.copy(selectedProfileId = null)),
        )
        verifyNoInteractions(snapshots, publisher, sessions)
    }

    private fun query(data: String) =
        TelegramWidgetQueryContext("q", 11L, 11L, 8, data, "data", true, false)

    private fun view(after: UUID? = null, nextCursor: UUID? = null) =
        ProfileActivityView(profileId, "Kirito", after, nextCursor, emptyList())
}
