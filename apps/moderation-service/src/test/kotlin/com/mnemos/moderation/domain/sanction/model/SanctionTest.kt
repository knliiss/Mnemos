package com.mnemos.moderation.domain.sanction.model

import com.mnemos.common.core.model.id.UserId
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Instant

class SanctionTest {
    @Test
    fun `create generates time ordered id`() {
        val sanction = createSanction()

        assertEquals(7, sanction.id.id.version())
        assertEquals(DomainSanctionStatus.ACTIVE, sanction.status)
    }

    @Test
    fun `revoke active sanction once`() {
        val sanction = createSanction()

        val first = sanction.revoke()
        val second = sanction.revoke()

        assertTrue(first)
        assertFalse(second)
        assertEquals(DomainSanctionStatus.REVOKED, sanction.status)
    }

    @Test
    fun `expire active sanction after expiration`() {
        val createdAt = Instant.parse("2026-08-13T16:00:00Z")
        val sanction =
            Sanction.create(
                userId = UserId.generate(),
                type = DomainSanctionType.BAN,
                reason = "Reason",
                internalNote = null,
                createdByUserId = UserId.generate(),
                createdAt = createdAt,
                expiresAt = createdAt.plusSeconds(60),
            )

        val expired = sanction.expire(createdAt.plusSeconds(60))

        assertTrue(expired)
        assertEquals(DomainSanctionStatus.EXPIRED, sanction.status)
    }

    private fun createSanction(): Sanction {
        val createdAt = Instant.parse("2026-08-13T16:00:00Z")

        return Sanction.create(
            userId = UserId.generate(),
            type = DomainSanctionType.NOTIFICATION_SUSPENSION,
            reason = "Reason",
            internalNote = "Internal",
            createdByUserId = UserId.generate(),
            createdAt = createdAt,
            expiresAt = createdAt.plusSeconds(3600),
        )
    }
}
