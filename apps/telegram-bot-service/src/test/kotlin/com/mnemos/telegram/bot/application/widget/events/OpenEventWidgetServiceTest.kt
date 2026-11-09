package com.mnemos.telegram.bot.application.widget.events

import com.mnemos.telegram.bot.application.events.model.EventHistoryFilter
import com.mnemos.telegram.bot.application.events.model.EventHistoryItem
import com.mnemos.telegram.bot.application.events.model.EventHistoryPage
import com.mnemos.telegram.bot.application.events.model.EventHistoryQuery
import com.mnemos.telegram.bot.application.events.model.EventHistoryResult
import com.mnemos.telegram.bot.application.events.port.out.EventHistoryGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.events.port.out.EventWidgetPublisher
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class OpenEventWidgetServiceTest {
    private val events = mock<EventHistoryGateway>()
    private val publisher = mock<EventWidgetPublisher>()

    @Test
    fun reusesDomainEventFeedAndMapsAllEventTypes() {
        val after = UUID.randomUUID()
        val next = UUID.randomUUID()
        val items =
            listOf(
                EventHistoryItem.ItemDrop(
                    eventId = UUID.randomUUID(),
                    occurredAt = Instant.parse("2026-11-09T19:00:00Z"),
                    itemId = UUID.randomUUID(),
                    itemName = "Клинок",
                    itemRarity = "SECRET",
                    itemType = "SWORD",
                    droppedFor = "Kirito",
                ),
                EventHistoryItem.Booster(
                    eventId = UUID.randomUUID(),
                    occurredAt = Instant.parse("2026-11-09T19:01:00Z"),
                    boosterType = "POWER",
                    activatedBy = "Asuna",
                ),
                EventHistoryItem.Global(
                    eventId = UUID.randomUUID(),
                    occurredAt = Instant.parse("2026-11-09T19:02:00Z"),
                    globalType = "MOON",
                ),
                EventHistoryItem.Raid(
                    eventId = UUID.randomUUID(),
                    occurredAt = Instant.parse("2026-11-09T19:03:00Z"),
                    locations = listOf("Ліс"),
                ),
            )
        whenever(events.getPage(EventHistoryQuery(EventHistoryFilter.ALL, after, telegramId = 11L)))
            .thenReturn(EventHistoryResult.Available(EventHistoryPage(items, next)))
        whenever(publisher.publish(eq(11L), any())).thenReturn(TelegramDeliveryResult.Delivered)
        val service = OpenEventWidgetService(TelegramWidgetProperties(enabled = true), events, publisher)

        assertEquals(TelegramDeliveryResult.Delivered, service.execute(11L, EventHistoryFilter.ALL, after))

        val view = argumentCaptor<EventWidgetView>()
        verify(publisher).publish(eq(11L), view.capture())
        assertEquals(after, view.firstValue.after)
        assertEquals(next, view.firstValue.nextCursor)
        assertEquals(listOf("Дроп", "Бустер", "Глобальна", "Рейд"), view.firstValue.items.map { it.type })
        assertTrue(view.firstValue.items.all { it.label.toByteArray(Charsets.UTF_8).size <= 36 })
    }

    @Test
    fun disabledFeatureNeverQueriesDomainService() {
        val service = OpenEventWidgetService(TelegramWidgetProperties(enabled = false), events, publisher)

        assertEquals(
            TelegramDeliveryResult.PermanentFailure("widgets-disabled"),
            service.execute(11L, EventHistoryFilter.ALL, null),
        )
        verifyNoInteractions(events, publisher)
    }
}
