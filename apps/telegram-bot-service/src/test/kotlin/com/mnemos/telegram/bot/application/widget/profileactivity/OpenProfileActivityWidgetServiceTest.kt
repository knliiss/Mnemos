package com.mnemos.telegram.bot.application.widget.profileactivity

import com.mnemos.telegram.bot.application.events.model.EventHistoryFilter
import com.mnemos.telegram.bot.application.events.model.EventHistoryItem
import com.mnemos.telegram.bot.application.events.model.EventHistoryPage
import com.mnemos.telegram.bot.application.events.model.EventHistoryQuery
import com.mnemos.telegram.bot.application.events.model.EventHistoryResult
import com.mnemos.telegram.bot.application.events.port.out.EventHistoryGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.profile.model.ProfilePresenceStatus
import com.mnemos.telegram.bot.application.profile.model.PublicProfileDetailResult
import com.mnemos.telegram.bot.application.profile.model.PublicProfileSummary
import com.mnemos.telegram.bot.application.profile.port.out.PublicProfileGateway
import com.mnemos.telegram.bot.application.widget.profileactivity.port.out.ProfileActivityWidgetPublisher
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class OpenProfileActivityWidgetServiceTest {
    private val profiles = mock<PublicProfileGateway>()
    private val events = mock<EventHistoryGateway>()
    private val publisher = mock<ProfileActivityWidgetPublisher>()
    private val service = OpenProfileActivityWidgetService(TelegramWidgetProperties(enabled = true), profiles, events, publisher)
    private val profileId = UUID.randomUUID()
    private val occurredAt = Instant.parse("2026-11-10T12:00:00Z")

    @Test
    fun activityUsesExactPlayerFilterAndShowsOnlyTheirDrops() {
        whenever(profiles.get(profileId)).thenReturn(PublicProfileDetailResult.Available(profile()))
        val event = drop("Kirito")
        whenever(events.getPage(EventHistoryQuery(EventHistoryFilter.ITEM_DROP, null, telegramId = 11L, droppedFor = "Kirito")))
            .thenReturn(EventHistoryResult.Available(EventHistoryPage(listOf(event, drop("NotKirito")), event.eventId)))

        val result = service.load(11L, profileId) as ProfileActivitySnapshotResult.Available

        assertEquals("Kirito", result.view.nickname)
        assertEquals(1, result.view.items.size)
        assertEquals("Довгий клинок", result.view.items.single().name)
        assertNull(result.view.nextCursor)
        verify(events).getPage(EventHistoryQuery(EventHistoryFilter.ITEM_DROP, null, telegramId = 11L, droppedFor = "Kirito"))
    }

    @Test
    fun fullPageProvidesCursorForPagination() {
        whenever(profiles.get(profileId)).thenReturn(PublicProfileDetailResult.Available(profile()))
        val drops = List(8) { drop("Kirito") }
        val cursor = drops.last().eventId
        whenever(events.getPage(EventHistoryQuery(EventHistoryFilter.ITEM_DROP, null, telegramId = 11L, droppedFor = "Kirito")))
            .thenReturn(EventHistoryResult.Available(EventHistoryPage(drops, cursor)))

        val result = service.load(11L, profileId) as ProfileActivitySnapshotResult.Available

        assertEquals(cursor, result.view.nextCursor)
        assertEquals(8, result.view.items.size)
    }

    @Test
    fun missingProfileDoesNotQueryEventService() {
        whenever(profiles.get(profileId)).thenReturn(PublicProfileDetailResult.NotFound)

        assertEquals(ProfileActivitySnapshotResult.NotFound, service.load(11L, profileId))
        verifyNoInteractions(events)
    }

    @Test
    fun disabledWidgetDoesNotQueryEitherBackend() {
        val disabled = OpenProfileActivityWidgetService(
            TelegramWidgetProperties(enabled = false), profiles, events, publisher,
        )
        assertEquals(
            TelegramDeliveryResult.PermanentFailure("widgets-disabled"),
            disabled.execute(11L, profileId),
        )
        verifyNoInteractions(profiles, events, publisher)
    }

    private fun profile() = PublicProfileSummary(
        accountId = profileId,
        nickname = "Kirito",
        cristalixUuid = UUID.randomUUID(),
        likes = 4,
        dislikes = 1,
        comments = 0,
        presenceStatus = ProfilePresenceStatus.UNKNOWN,
        realm = null,
        lastSeenOnline = null,
        createdAt = occurredAt,
    )

    private fun drop(nickname: String) = EventHistoryItem.ItemDrop(
        eventId = UUID.randomUUID(),
        occurredAt = occurredAt,
        itemId = UUID.randomUUID(),
        itemName = "Довгий клинок",
        itemRarity = "SECRET",
        itemType = "SWORD",
        droppedFor = nickname,
    )
}
