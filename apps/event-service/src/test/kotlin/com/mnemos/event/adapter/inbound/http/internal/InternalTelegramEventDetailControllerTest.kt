package com.mnemos.event.adapter.inbound.http.internal

import com.mnemos.event.application.feed.model.GlobalFeedItem
import com.mnemos.event.application.feed.usecase.get.GetEventFeedItemService
import com.mnemos.event.contracts.internal.v1.InternalEventFeedType
import com.mnemos.event.domain.EventId
import com.mnemos.event.domain.global.model.DomainGlobalEventType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import org.springframework.security.access.prepost.PreAuthorize
import java.time.Instant
import java.util.UUID

class InternalTelegramEventDetailControllerTest {
    private val service = mock<GetEventFeedItemService>()
    private val controller = InternalTelegramEventDetailController(service)

    @Test
    fun returnsNotFoundForUnknownEvent() {
        val id = UUID.randomUUID()
        whenever(service.execute(EventId(id))).thenReturn(null)

        assertEquals(404, controller.get(id).statusCode.value())
    }

    @Test
    fun returnsExistingTypedProjectionWithoutDuplicatingEventMapping() {
        val id = UUID.randomUUID()
        val at = Instant.parse("2026-11-10T12:00:00Z")
        whenever(service.execute(EventId(id))).thenReturn(
            GlobalFeedItem(
                eventId = EventId(id),
                occurredAt = at,
                globalType = DomainGlobalEventType.MOON,
            ),
        )

        val result = controller.get(id)

        assertEquals(200, result.statusCode.value())
        assertEquals(id, result.body?.eventId)
        assertEquals(InternalEventFeedType.GLOBAL, result.body?.type)
        assertEquals(at, result.body?.occurredAt)
    }

    @Test
    fun internalControllerIsRestrictedToAuthenticatedTelegramService() {
        val guard = InternalTelegramEventDetailController::class.java.getAnnotation(PreAuthorize::class.java)

        assertNotNull(guard)
        assertEquals("authentication.name == 'telegram-bot-service'", guard.value)
    }
}
