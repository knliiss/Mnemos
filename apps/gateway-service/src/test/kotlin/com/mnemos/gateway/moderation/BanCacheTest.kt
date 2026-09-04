package com.mnemos.gateway.moderation

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class BanCacheTest {
    @Test
    fun `deactivation of old sanction does not remove newer ban`() {
        val now = Instant.parse("2026-08-13T18:00:00Z")
        val cache = BanCache(Clock.fixed(now, ZoneOffset.UTC))
        val userId = UUID.fromString("0198f6ce-e934-7000-8000-000000000001")
        val oldSanctionId = UUID.fromString("0198f6ce-e934-7000-8000-000000000002")
        val newSanctionId = UUID.fromString("0198f6ce-e934-7000-8000-000000000003")
        val oldOccurredAt = Instant.parse("2026-08-13T16:00:00Z")
        val newOccurredAt = Instant.parse("2026-08-13T17:00:00Z")

        cache.activate(
            sanctionId = oldSanctionId,
            userId = userId,
            reason = "Old reason",
            occurredAt = oldOccurredAt,
            expiresAt = null,
        )
        cache.activate(
            sanctionId = newSanctionId,
            userId = userId,
            reason = "New reason",
            occurredAt = newOccurredAt,
            expiresAt = null,
        )
        cache.deactivate(
            sanctionId = oldSanctionId,
            userId = userId,
            occurredAt = newOccurredAt.plusSeconds(60),
        )

        val activeBan = cache.findActiveBan(userId)

        assertTrue(cache.isBanned(userId))
        assertEquals(newSanctionId, activeBan?.sanctionId)
        assertEquals("New reason", activeBan?.reason)
    }

    @Test
    fun `deactivation removes matching active sanction`() {
        val now = Instant.parse("2026-08-13T18:00:00Z")
        val cache = BanCache(Clock.fixed(now, ZoneOffset.UTC))
        val userId = UUID.fromString("0198f6ce-e934-7000-8000-000000000001")
        val sanctionId = UUID.fromString("0198f6ce-e934-7000-8000-000000000002")
        val occurredAt = Instant.parse("2026-08-13T16:00:00Z")

        cache.activate(
            sanctionId = sanctionId,
            userId = userId,
            reason = "Reason",
            occurredAt = occurredAt,
            expiresAt = null,
        )
        cache.deactivate(
            sanctionId = sanctionId,
            userId = userId,
            occurredAt = occurredAt.plusSeconds(60),
        )

        assertFalse(cache.isBanned(userId))
    }

    @Test
    fun `expired sanction is not treated as an active ban`() {
        val now = Instant.parse("2026-08-13T18:00:00Z")
        val cache = BanCache(Clock.fixed(now, ZoneOffset.UTC))
        val userId = UUID.fromString("0198f6ce-e934-7000-8000-000000000001")
        val sanctionId = UUID.fromString("0198f6ce-e934-7000-8000-000000000002")

        cache.activate(
            sanctionId = sanctionId,
            userId = userId,
            reason = "Expired reason",
            occurredAt = Instant.parse("2026-08-13T16:00:00Z"),
            expiresAt = now,
        )

        assertFalse(cache.isBanned(userId))
        assertNull(cache.findActiveBan(userId))
    }
}
