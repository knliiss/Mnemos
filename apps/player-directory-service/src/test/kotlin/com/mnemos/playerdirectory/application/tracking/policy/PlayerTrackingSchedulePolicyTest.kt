package com.mnemos.playerdirectory.application.tracking.policy

import com.mnemos.playerdirectory.domain.player.model.PlayerPresenceStatus
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Duration
import java.time.Instant

class PlayerTrackingSchedulePolicyTest {
    private val policy =
        PlayerTrackingSchedulePolicy(
            subscriptionPollInterval = Duration.ofSeconds(20),
            unverifiablePollInterval = Duration.ofMinutes(2),
            failureBackoff = Duration.ofSeconds(30),
            maximumFailureBackoff = Duration.ofMinutes(10),
        )
    private val now = Instant.parse("2026-08-27T00:00:00Z")

    @Test
    fun `verified presence keeps a short subscription poll interval`() {
        assertEquals(
            now.plusSeconds(20),
            policy.nextAfterObservation(PlayerPresenceStatus.ONLINE, now),
        )
        assertEquals(
            now.plusSeconds(20),
            policy.nextAfterObservation(PlayerPresenceStatus.OFFLINE, now),
        )
    }

    @Test
    fun `unverifiable presence is polled less aggressively`() {
        assertEquals(
            now.plus(Duration.ofMinutes(2)),
            policy.nextAfterObservation(PlayerPresenceStatus.UNKNOWN, now),
        )
        assertEquals(
            now.plus(Duration.ofMinutes(2)),
            policy.nextAfterObservation(PlayerPresenceStatus.HIDDEN, now),
        )
    }

    @Test
    fun `failure backoff grows exponentially and is capped`() {
        assertEquals(now.plusSeconds(30), policy.nextAfterFailure(0, now))
        assertEquals(now.plusSeconds(60), policy.nextAfterFailure(1, now))
        assertEquals(now.plus(Duration.ofMinutes(10)), policy.nextAfterFailure(20, now))
    }
}
