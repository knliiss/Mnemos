package com.mnemos.telegram.bot.application.widget.eventdetail

import com.mnemos.telegram.bot.application.events.model.EventDetailResult
import com.mnemos.telegram.bot.application.events.model.EventHistoryItem
import com.mnemos.telegram.bot.application.events.port.out.EventDetailGateway
import com.mnemos.telegram.bot.application.message.model.TelegramDeliveryResult
import com.mnemos.telegram.bot.application.widget.eventdetail.port.out.EventDetailWidgetPublisher
import com.mnemos.telegram.bot.configuration.widget.TelegramWidgetProperties
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import java.time.Instant
import java.util.UUID

class OpenEventDetailWidgetServiceTest {
    private val events = mock<EventDetailGateway>()
    private val publisher = mock<EventDetailWidgetPublisher>()
    private val service = OpenEventDetailWidgetService(TelegramWidgetProperties(enabled = true), events, publisher)
    private val id = UUID.randomUUID()

    @Test
    fun mapsFullItemDropPropertiesWithoutCopyingDomainLogic() {
        whenever(events.get(id)).thenReturn(EventDetailResult.Available(
            EventHistoryItem.ItemDrop(
                eventId = id,
                occurredAt = Instant.parse("2026-11-10T12:20:00Z"),
                itemId = UUID.randomUUID(),
                itemName = "Клинок",
                itemRarity = "SECRET",
                itemType = "SWORD",
                droppedFor = "Kirito",
            ),
        ))
        whenever(publisher.publish(eq(11L), any())).thenReturn(TelegramDeliveryResult.Delivered)

        assertEquals(TelegramDeliveryResult.Delivered, service.execute(11L, id))
        val captor = org.mockito.kotlin.argumentCaptor<EventDetailView>()
        verify(publisher).publish(eq(11L), captor.capture())
        assertEquals("Клинок", captor.firstValue.title)
        assertEquals("Дроп", captor.firstValue.type)
        assertEquals("Kirito", captor.firstValue.facts.first().value)
    }

    @Test
    fun missingEventDoesNotPublishAnEmptyWidget() {
        whenever(events.get(id)).thenReturn(EventDetailResult.NotFound)

        assertEquals(TelegramDeliveryResult.PermanentFailure("event-not-found"), service.execute(11L, id))
        verifyNoInteractions(publisher)
    }

    @Test
    fun disabledWidgetCannotAccessTheEventGateway() {
        val disabled = OpenEventDetailWidgetService(TelegramWidgetProperties(enabled = false), events, publisher)

        assertEquals(TelegramDeliveryResult.PermanentFailure("widgets-disabled"), disabled.execute(11L, id))
        verifyNoInteractions(events, publisher)
    }
}
