package com.mnemos.event.application.booster.mapper

import com.mnemos.event.application.booster.mapper.BoosterIntegrationEventMapper.toIntegrationEvent
import com.mnemos.event.contracts.EventEventType
import com.mnemos.event.domain.booster.model.BoosterEvent
import com.mnemos.event.domain.booster.model.DomainBoosterType
import com.mnemos.event.domain.player.model.CristalixPlayerId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class BoosterIntegrationEventMapperTest {
    @Test
    fun `publishes booster contract type with player identity`() {
        val playerId = UUID.fromString("019c1129-ef54-7000-8000-000000000111")
        val event =
            BoosterEvent(
                type = DomainBoosterType.LUCK,
                playerId = CristalixPlayerId(playerId),
                activatedBy = "knaliz",
                activatedAt = Instant.parse("2026-08-24T22:00:00Z"),
            )

        val integrationEvent = event.toIntegrationEvent()

        assertEquals(EventEventType.BOOSTER_ACTIVATED, integrationEvent.type)
        assertEquals(playerId, integrationEvent.payload.playerId)
        assertEquals("knaliz", integrationEvent.payload.activatedBy)
    }
}
