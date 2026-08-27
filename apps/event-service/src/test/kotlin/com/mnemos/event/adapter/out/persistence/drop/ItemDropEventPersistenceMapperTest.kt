package com.mnemos.event.adapter.out.persistence.drop

import com.mnemos.event.adapter.out.persistence.drop.ItemDropEventPersistenceMapper.toDomain
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class ItemDropEventPersistenceMapperTest {
    @Test
    fun `maps persisted item id instead of event id`() {
        val eventId = UUID.fromString("019c1129-ef54-7000-8000-000000000101")
        val itemId = UUID.fromString("019c1129-ef54-7000-8000-000000000102")
        val playerId = UUID.fromString("019c1129-ef54-7000-8000-000000000103")
        val entity =
            ItemDropEventEntity(
                id = eventId,
                itemId = itemId,
                playerId = playerId,
                droppedFor = "knaliz",
                occurredAt = Instant.parse("2026-08-24T22:00:00Z"),
                identityResolutionPending = false,
                identityResolutionRequestedAt = null,
            )

        val event = entity.toDomain()

        assertEquals(eventId, event.id.id)
        assertEquals(itemId, event.itemId.id)
        assertEquals(playerId, event.playerId?.id)
        assertNull(event.identityResolutionRequestedAt)
    }
}
