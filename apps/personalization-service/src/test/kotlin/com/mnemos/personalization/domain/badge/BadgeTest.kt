package com.mnemos.personalization.domain.badge

import com.mnemos.personalization.domain.badge.model.Badge
import com.mnemos.personalization.domain.personalization.model.HexColor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant

class BadgeTest {
    @Test
    fun `creates a valid system badge`() {
        val createdAt = Instant.parse("2026-08-15T00:00:00Z")

        val badge =
            Badge.create(
                code = "MODERATOR",
                name = "Moderator",
                icon = "🛡️",
                color = HexColor("#8ACBFF"),
                now = createdAt,
            )

        assertEquals("MODERATOR", badge.code)
        assertEquals(createdAt, badge.createdAt)
    }

    @Test
    fun `rejects invalid badge code`() {
        assertThrows(IllegalArgumentException::class.java) {
            Badge.create(
                code = "moderator badge",
                name = "Moderator",
                icon = "🛡️",
                color = HexColor("#8ACBFF"),
                now = Instant.parse("2026-08-15T00:00:00Z"),
            )
        }
    }
}
