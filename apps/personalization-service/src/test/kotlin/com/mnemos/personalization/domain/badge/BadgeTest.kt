package com.mnemos.personalization.domain.badge

import com.mnemos.personalization.domain.badge.model.Badge
import com.mnemos.personalization.domain.badge.model.BadgeId
import com.mnemos.personalization.domain.personalization.model.HexColor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant

class BadgeTest {
    private val createdAt = Instant.parse("2026-08-15T00:00:00Z")

    @Test
    fun `creates a valid system badge with deterministic image object key`() {
        val badge =
            Badge.create(
                code = "MODERATOR",
                name = "Moderator",
                color = HexColor("#8ACBFF"),
                now = createdAt,
            )

        assertEquals("MODERATOR", badge.code)
        assertEquals("badges/${badge.id.id}.png", badge.imageObjectKey)
        assertEquals(createdAt, badge.createdAt)
    }

    @Test
    fun `rejects invalid badge code`() {
        assertThrows(IllegalArgumentException::class.java) {
            Badge.create(
                code = "moderator badge",
                name = "Moderator",
                color = HexColor("#8ACBFF"),
                now = createdAt,
            )
        }
    }

    @Test
    fun `rejects badge name longer than persistence limit`() {
        assertThrows(IllegalArgumentException::class.java) {
            Badge.create(
                code = "LONG_NAME",
                name = "x".repeat(65),
                color = HexColor("#8ACBFF"),
                now = createdAt,
            )
        }
    }

    @Test
    fun `rejects invalid badge image object key`() {
        assertThrows(IllegalArgumentException::class.java) {
            Badge(
                id = BadgeId.generate(),
                code = "INVALID_IMAGE",
                name = "Invalid image",
                imageObjectKey = "../badge.png",
                color = HexColor("#8ACBFF"),
                createdAt = createdAt,
            )
        }
    }
}
