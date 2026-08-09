package com.mnemos.messaging.outbox.publisher

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class OutboxRetryPolicyTest {

    private val now =
        Instant.parse("2026-07-27T18:00:00Z")

    private val policy =
        OutboxRetryPolicy(
            maxAttempts = 4,
            initialBackoff = Duration.ofSeconds(1),
            maxBackoff = Duration.ofSeconds(3),
        )

    @Test
    fun `first failure retries after initial backoff`() {
        val decision =
            policy.afterFailure(
                previousFailureCount = 0,
                now = now,
            )

        assertEquals(
            1,
            decision.failureCount,
        )
        assertFalse(decision.dead)
        assertEquals(
            now.plusSeconds(1),
            decision.nextAttemptAt,
        )
    }

    @Test
    fun `backoff grows exponentially and is capped`() {
        assertEquals(
            now.plusSeconds(2),
            policy.afterFailure(
                previousFailureCount = 1,
                now = now,
            ).nextAttemptAt,
        )

        assertEquals(
            now.plusSeconds(3),
            policy.afterFailure(
                previousFailureCount = 2,
                now = now,
            ).nextAttemptAt,
        )
    }

    @Test
    fun `event becomes dead after maximum failures`() {
        val decision =
            policy.afterFailure(
                previousFailureCount = 3,
                now = now,
            )

        assertEquals(
            4,
            decision.failureCount,
        )
        assertTrue(decision.dead)
        assertNull(decision.nextAttemptAt)
    }
}