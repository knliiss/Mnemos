package com.mnemos.event.adapter.out.persistence.drop.query

import com.mnemos.event.adapter.out.persistence.drop.query.ItemDropQueryMapper.toView
import com.mnemos.event.domain.item.model.DomainItemRarity
import com.mnemos.event.domain.item.model.DomainItemType
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class ItemDropQueryMapperTest {
    @Test
    fun `maps native scalar item values and player identity into domain view`() {
        val playerId = UUID.fromString("019c1129-ef54-7000-8000-000000000092")
        val projection =
            ItemDropViewProjection(
                id = UUID.fromString("019c1129-ef54-7000-8000-000000000090"),
                itemId = UUID.fromString("019c1129-ef54-7000-8000-000000000091"),
                itemName = "Frozen Reaper",
                itemType = "SWORD",
                itemRarity = "MYTHICAL",
                playerId = playerId,
                droppedFor = "knaliz",
                occurredAt = Instant.parse("2026-08-24T22:00:00Z"),
            )

        val view = projection.toView()

        assertEquals(DomainItemType.SWORD, view.itemType)
        assertEquals(DomainItemRarity.MYTHICAL, view.itemRarity)
        assertEquals("Frozen Reaper", view.itemName)
        assertEquals(playerId, view.playerId?.id)
        assertEquals("knaliz", view.droppedFor)
    }
}
