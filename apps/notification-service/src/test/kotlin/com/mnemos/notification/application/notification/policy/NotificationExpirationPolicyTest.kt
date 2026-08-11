package com.mnemos.notification.application.notification.policy

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class NotificationExpirationPolicyTest {
    @Test
    fun `expiration is calculated from occurrence time`() {
        val policy = NotificationExpirationPolicy(Duration.ofMinutes(5))
        assertEquals(
            Instant.parse("2026-08-11T01:25:00Z"),
            policy.expiresAt(Instant.parse("2026-08-11T01:20:00Z")),
        )
    }

    @Test
    fun `non positive ttl is rejected`() {
        assertThrows(IllegalArgumentException::class.java) { NotificationExpirationPolicy(Duration.ZERO) }
        assertThrows(IllegalArgumentException::class.java) { NotificationExpirationPolicy(Duration.ofSeconds(-1)) }
    }
}
