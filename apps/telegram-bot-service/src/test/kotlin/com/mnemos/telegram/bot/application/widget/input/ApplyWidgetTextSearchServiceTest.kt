package com.mnemos.telegram.bot.application.widget.input

import com.mnemos.telegram.bot.application.events.model.EventHistoryFilter
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.events.EventWidgetSnapshotQuery
import com.mnemos.telegram.bot.application.widget.events.EventWidgetSnapshotResult
import com.mnemos.telegram.bot.application.widget.events.EventWidgetView
import com.mnemos.telegram.bot.application.widget.events.port.out.EventWidgetPublisher
import com.mnemos.telegram.bot.application.widget.online.OnlineWidgetSnapshotQuery
import com.mnemos.telegram.bot.application.widget.online.OnlineWidgetSnapshotResult
import com.mnemos.telegram.bot.application.widget.online.OnlineWidgetView
import com.mnemos.telegram.bot.application.widget.online.port.out.OnlineWidgetPublisher
import com.mnemos.telegram.bot.application.widget.profiles.ProfileWidgetSnapshotQuery
import com.mnemos.telegram.bot.application.widget.profiles.ProfileWidgetSnapshotResult
import com.mnemos.telegram.bot.application.widget.profiles.ProfileWidgetView
import com.mnemos.telegram.bot.application.widget.profiles.port.out.ProfileWidgetPublisher
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

class ApplyWidgetTextSearchServiceTest {
    private val sessions = mock<TelegramWidgetSessionRepository>()
    private val events = mock<EventWidgetSnapshotQuery>()
    private val eventPublisher = mock<EventWidgetPublisher>()
    private val online = mock<OnlineWidgetSnapshotQuery>()
    private val onlinePublisher = mock<OnlineWidgetPublisher>()
    private val profiles = mock<ProfileWidgetSnapshotQuery>()
    private val profilePublisher = mock<ProfileWidgetPublisher>()
    private val service = ApplyWidgetTextSearchService(sessions, events, eventPublisher, online, onlinePublisher, profiles, profilePublisher)

    @Test
    fun eventSearchUpdatesExistingWidgetAndPersistsSearchContext() {
        val session = TelegramWidgetSession(11L, 8, TelegramWidgetScreen.EVENTS, null, EventHistoryFilter.ALL.ordinal)
        val view = EventWidgetView(EventHistoryFilter.ALL, null, null, emptyList(), "Kirito")
        whenever(events.load(11L, EventHistoryFilter.ALL, null, "Kirito"))
            .thenReturn(EventWidgetSnapshotResult.Available(view))
        whenever(eventPublisher.update(11L, 8, view)).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, service.apply(session, "Kirito"))

        verify(eventPublisher).update(11L, 8, view)
        verify(sessions).save(session.copy(cursor = null, search = "Kirito"))
        verifyNoInteractions(onlinePublisher)
    }

    @Test
    fun temporaryWidgetDeliveryFailureDoesNotAdvanceSearchSession() {
        val session = TelegramWidgetSession(11L, 8, TelegramWidgetScreen.EVENTS, null)
        val view = EventWidgetView(EventHistoryFilter.ALL, null, null, emptyList(), "Kirito")
        whenever(events.load(11L, EventHistoryFilter.ALL, null, "Kirito"))
            .thenReturn(EventWidgetSnapshotResult.Available(view))
        whenever(eventPublisher.update(11L, 8, view)).thenReturn(TelegramDeliveryResult.Retryable)

        assertEquals(TelegramDeliveryResult.Retryable, service.apply(session, "Kirito"))
        verifyNoInteractions(sessions)
    }

    @Test
    fun onlineSearchUsesSnapshotAndUpdatesOwningMessage() {
        val session = TelegramWidgetSession(11L, 8, TelegramWidgetScreen.ONLINE, null, 2)
        val view = OnlineWidgetView(120, 2, null, null, 1, "19:00 UTC", emptyList(), emptyList(), "Kiri")
        whenever(online.load(11L, null, "Kiri")).thenReturn(OnlineWidgetSnapshotResult.Available(view))
        whenever(onlinePublisher.update(11L, 8, view)).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, service.apply(session, "Kiri"))
        verify(sessions).save(session.copy(page = 0, cursor = null, search = "Kiri"))
        verifyNoInteractions(eventPublisher)
    }

    @Test
    fun profileSearchReusesTypedGatewayAndResetsPreviousSelectedProfile() {
        val previous = java.util.UUID.randomUUID()
        val session = TelegramWidgetSession(11L, 8, TelegramWidgetScreen.PROFILES, null, search = "Old", selectedProfileId = previous)
        val view = ProfileWidgetView("Kirito", 0, 1, 0, emptyList())
        whenever(profiles.load(11L, "Kirito", 0, null))
            .thenReturn(ProfileWidgetSnapshotResult.Available(view))
        whenever(profilePublisher.update(11L, 8, view)).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, service.apply(session, "Kirito"))
        verify(profilePublisher).update(11L, 8, view)
        verify(sessions).save(session.copy(page = 0, cursor = null, search = "Kirito", selectedProfileId = null))
    }

    @Test
    fun rejectsUnrelatedWidgetScreenWithoutExternalCalls() {
        val session = TelegramWidgetSession(11L, 8, TelegramWidgetScreen.WALLET, null)
        assertEquals(TelegramDeliveryResult.PermanentFailure("unsupported-widget-search"), service.apply(session, "Kirito"))
        verifyNoInteractions(events, eventPublisher, online, onlinePublisher, sessions)
    }
}
