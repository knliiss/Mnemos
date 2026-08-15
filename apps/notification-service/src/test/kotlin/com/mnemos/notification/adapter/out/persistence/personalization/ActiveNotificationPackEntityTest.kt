package com.mnemos.notification.adapter.out.persistence.personalization

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class ActiveNotificationPackEntityTest {
    @Test
    fun `newer revision applies and older revision is rejected after clear`() {
        val state = tombstone()
        val selectedAt = Instant.parse("2026-08-15T15:10:00Z")
        val clearedAt = Instant.parse("2026-08-15T15:11:00Z")

        assertTrue(state.shouldApply(1))

        state.select(
            personalizationId = PERSONALIZATION_ID,
            mixedAggregateTitle = "Mixed events",
            templates = mapOf("RAID" to emptyMap<String, Any?>()),
            selectionRevision = 1,
            eventId = SELECTED_EVENT_ID,
            occurredAt = selectedAt,
        )

        assertFalse(state.shouldApply(1))
        assertTrue(state.shouldApply(2))

        state.clear(
            selectionRevision = 2,
            eventId = CLEARED_EVENT_ID,
            occurredAt = clearedAt,
        )

        assertNull(state.personalizationId)
        assertNull(state.mixedAggregateTitle)
        assertNull(state.templates)
        assertFalse(state.shouldApply(1))
        assertFalse(state.shouldApply(2))
        assertTrue(state.shouldApply(3))
    }

    private fun tombstone(): ActiveNotificationPackEntity =
        ActiveNotificationPackEntity(
            userId = USER_ID,
            personalizationId = null,
            mixedAggregateTitle = null,
            templates = null,
            lastRevision = 0,
            lastEventId = ZERO_ID,
            lastEventAt = Instant.EPOCH,
        )

    private companion object {
        val USER_ID: UUID = UUID.fromString("019c1129-ef54-7000-8000-000000000032")
        val PERSONALIZATION_ID: UUID = UUID.fromString("019c1129-ef54-7000-8000-000000000033")
        val SELECTED_EVENT_ID: UUID = UUID.fromString("019c1129-ef54-7000-8000-000000000034")
        val CLEARED_EVENT_ID: UUID = UUID.fromString("019c1129-ef54-7000-8000-000000000035")
        val ZERO_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")
    }
}
