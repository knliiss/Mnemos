package com.mnemos.gateway.moderation

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class BanCacheTest {
    @Test
    fun `deactivation of old sanction does not remove newer ban`() {
        val cache = BanCache()
        val userId = UUID.fromString("0198f6ce-e934-7000-8000-000000000001")
        val oldSanctionId = UUID.fromString("0198f6ce-e934-7000-8000-000000000002")
        val newSanctionId = UUID.fromString("0198f6ce-e934-7000-8000-000000000003")
        val oldOccurredAt = Instant.parse("2026-08-13T16:00:00Z")
        val newOccurredAt = Instant.parse("2026-08-13T17:00:00Z")

        cache.activate(
            sanctionId = oldSanctionId,
            userId = userId,
            occurredAt = oldOccurredAt,
        )
        cache.activate(
            sanctionId = newSanctionId,
            userId = userId,
            occurredAt = newOccurredAt,
        )
        cache.deactivate(
            sanctionId = oldSanctionId,
            userId = userId,
            occurredAt = newOccurredAt.plusSeconds(60),
        )

        assertTrue(cache.isBanned(userId))
    }

    @Test
    fun `deactivation removes matching active sanction`() {
        val cache = BanCache()
        val userId = UUID.fromString("0198f6ce-e934-7000-8000-000000000001")
        val sanctionId = UUID.fromString("0198f6ce-e934-7000-8000-000000000002")
        val occurredAt = Instant.parse("2026-08-13T16:00:00Z")

        cache.activate(
            sanctionId = sanctionId,
            userId = userId,
            occurredAt = occurredAt,
        )
        cache.deactivate(
            sanctionId = sanctionId,
            userId = userId,
            occurredAt = occurredAt.plusSeconds(60),
        )

        assertFalse(cache.isBanned(userId))
    }
}
