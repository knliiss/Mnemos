package com.mnemos.notification.adapter.out.persistence.personalization

import com.mnemos.notification.adapter.out.persistence.personalization.ActiveNotificationPackPersistenceMapper.toDomainOrNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class ActiveNotificationPackPersistenceMapperTest {
    @Test
    fun `cleared projection maps to no active pack`() {
        assertNull(tombstone().toDomainOrNull())
    }

    @Test
    fun `partial persisted snapshot is rejected`() {
        val entity =
            tombstone().apply {
                personalizationId = PERSONALIZATION_ID
            }

        assertThrows(IllegalStateException::class.java) {
            entity.toDomainOrNull()
        }
    }

    @Test
    fun `invalid persisted template document is rejected`() {
        val entity =
            ActiveNotificationPackEntity(
                userId = USER_ID,
                personalizationId = PERSONALIZATION_ID,
                mixedAggregateTitle = "Events",
                templates = mapOf("RAID" to mapOf("soloTitle" to 123)),
                lastRevision = 1,
                lastEventId = EVENT_ID,
                lastEventAt = Instant.parse("2026-08-15T19:40:00Z"),
            )

        assertThrows(IllegalStateException::class.java) {
            entity.toDomainOrNull()
        }
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
        val USER_ID: UUID = UUID.fromString("019c1195-0000-7000-8000-000000000001")
        val PERSONALIZATION_ID: UUID = UUID.fromString("019c1195-0000-7000-8000-000000000002")
        val EVENT_ID: UUID = UUID.fromString("019c1195-0000-7000-8000-000000000003")
        val ZERO_ID: UUID = UUID.fromString("00000000-0000-0000-0000-000000000000")
    }
}
