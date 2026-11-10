package com.mnemos.telegram.bot.application.widget.profiles

import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.profile.model.ProfilePresenceStatus
import com.mnemos.telegram.bot.application.widget.input.StartWidgetTextInputUseCase
import com.mnemos.telegram.bot.application.widget.input.WidgetTextInputPurpose
import com.mnemos.telegram.bot.application.widget.profiles.port.out.ProfileWidgetPublisher
import com.mnemos.telegram.bot.application.widget.profileactivity.OpenProfileActivityWidgetUseCase
import com.mnemos.telegram.bot.application.widget.profilecomments.OpenProfileCommentsWidgetUseCase
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

class ProfileWidgetActionHandlerTest {
    private val snapshots = mock<ProfileWidgetSnapshotQuery>()
    private val publisher = mock<ProfileWidgetPublisher>()
    private val sessions = mock<TelegramWidgetSessionRepository>()
    private val responder = mock<WidgetQueryResponder>()
    private val input = mock<StartWidgetTextInputUseCase>()
    private val activity = mock<OpenProfileActivityWidgetUseCase>()
    private val comments = mock<OpenProfileCommentsWidgetUseCase>()
    private val handler = ProfileWidgetActionHandler(snapshots, publisher, sessions, responder, input, activity, comments)
    private val accountId = UUID.randomUUID()
    private val session = TelegramWidgetSession(11L, 8, TelegramWidgetScreen.PROFILES, null, search = "Kirito")

    @Test
    fun searchActionOnlyAcknowledgesAndStartsTypedPrompt() {
        whenever(responder.reply(eq("q"), eq("INPUT"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:prof:search"), session))
        verify(input).start(session, WidgetTextInputPurpose.PUBLIC_PROFILE_SEARCH)
        verifyNoInteractions(snapshots)
    }

    @Test
    fun openingAResultRequiresVisibleAccountId() {
        whenever(snapshots.load(11L, "Kirito", 0, null))
            .thenReturn(ProfileWidgetSnapshotResult.Available(view()))
        whenever(responder.reply(eq("q"), eq("CONFLICT"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(
            TelegramDeliveryResult.Delivered,
            handler.handle(query("w1:prof:open:" + UUID.randomUUID()), session),
        )
        verifyNoInteractions(publisher, sessions)
    }

    @Test
    fun openingVisibleProfilePersistsSelectedId() {
        whenever(snapshots.load(11L, "Kirito", 0, null))
            .thenReturn(ProfileWidgetSnapshotResult.Available(view()))
        whenever(snapshots.load(11L, "Kirito", 0, accountId))
            .thenReturn(ProfileWidgetSnapshotResult.Available(view().copy(selected = entry())))
        whenever(publisher.state(any())).thenReturn("{}")
        whenever(responder.reply(eq("q"), eq("OK"), any(), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:prof:open:$accountId"), session))
        verify(sessions).save(session.copy(selectedProfileId = accountId))
        verify(publisher).update(eq(11L), eq(8), any())
    }

    @Test
    fun activityOpensOnlyForTheSelectedProfile() {
        val chosen = session.copy(selectedProfileId = accountId)
        whenever(snapshots.load(11L, "Kirito", 0, accountId))
            .thenReturn(ProfileWidgetSnapshotResult.Available(view().copy(selected = entry())))
        whenever(activity.execute(11L, accountId)).thenReturn(TelegramDeliveryResult.Delivered)
        whenever(responder.reply(eq("q"), eq("OK"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:prof:activity"), chosen))
        verify(activity).execute(11L, accountId)
    }

    @Test
    fun commentsOpenOnlyFromSelectedProfile() {
        val chosen = session.copy(selectedProfileId = accountId)
        whenever(snapshots.load(11L, "Kirito", 0, accountId))
            .thenReturn(ProfileWidgetSnapshotResult.Available(view().copy(selected = entry())))
        whenever(comments.execute(11L, accountId)).thenReturn(TelegramDeliveryResult.Delivered)
        whenever(responder.reply(eq("q"), eq("OK"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:prof:comments"), chosen))
        verify(comments).execute(11L, accountId)
        verifyNoInteractions(activity)
    }

    @Test
    fun commentsRequireAnExistingSelectedProfile() {
        whenever(snapshots.load(11L, "Kirito", 0, null))
            .thenReturn(ProfileWidgetSnapshotResult.Available(view()))
        whenever(responder.reply(eq("q"), eq("CONFLICT"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:prof:comments"), session))
        verifyNoInteractions(comments)
    }

    @Test
    fun rejectsStalePageNumbers() {
        whenever(snapshots.load(11L, "Kirito", 0, null))
            .thenReturn(ProfileWidgetSnapshotResult.Available(view().copy(totalPages = 3)))
        whenever(responder.reply(eq("q"), eq("CONFLICT"), any(), isNull()))
            .thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, handler.handle(query("w1:prof:next:2"), session))
        verifyNoInteractions(publisher, sessions)
    }

    private fun query(data: String) = TelegramWidgetQueryContext("q", 11L, 11L, 8, data, "data", true, false)

    private fun view() = ProfileWidgetView("Kirito", 0, 1, 1, listOf(entry()))

    private fun entry() = ProfileWidgetEntry(
        accountId, "Kirito", 4, 2, 3, ProfilePresenceStatus.HIDDEN,
        null, null, "2026-11-10",
    )
}
