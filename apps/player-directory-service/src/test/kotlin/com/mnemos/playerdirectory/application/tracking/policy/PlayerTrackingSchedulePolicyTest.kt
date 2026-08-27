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
            saoOnlinePollInterval = Duration.ofSeconds(30),
            highActivityPollInterval = Duration.ofMinutes(1),
            lowActivityPollInterval = Duration.ofMinutes(5),
            unverifiablePollInterval = Duration.ofMinutes(2),
            failureBackoff = Duration.ofSeconds(30),
            maximumFailureBackoff = Duration.ofMinutes(10),
        )
    private val now = Instant.parse("2026-08-27T00:00:00Z")

    @Test
    fun `subscription keeps the shortest poll interval for observable presence`() {
        assertEquals(
            now.plusSeconds(20),
            policy.nextAfterObservation(
                status = PlayerPresenceStatus.ONLINE,
                saoOnline = true,
                hasSubscription = true,
                highActivity = true,
                completedAt = now,
            ),
        )
    }

    @Test
    fun `hidden subscription falls back to unverifiable poll interval`() {
        assertEquals(
            now.plus(Duration.ofMinutes(2)),
            policy.nextAfterObservation(
                status = PlayerPresenceStatus.HIDDEN,
                saoOnline = false,
                hasSubscription = true,
                highActivity = false,
                completedAt = now,
            ),
        )
    }

    @Test
    fun `sao online player uses sao poll interval without subscription`() {
        assertEquals(
            now.plusSeconds(30),
            policy.nextAfterObservation(
                status = PlayerPresenceStatus.ONLINE,
                saoOnline = true,
                hasSubscription = false,
                highActivity = true,
                completedAt = now,
            ),
        )
    }

    @Test
    fun `high activity player uses high activity poll interval`() {
        assertEquals(
            now.plus(Duration.ofMinutes(1)),
            policy.nextAfterObservation(
                status = PlayerPresenceStatus.OFFLINE,
                saoOnline = false,
                hasSubscription = false,
                highActivity = true,
                completedAt = now,
            ),
        )
    }

    @Test
    fun `low activity player uses low activity poll interval`() {
        assertEquals(
            now.plus(Duration.ofMinutes(5)),
            policy.nextAfterObservation(
                status = PlayerPresenceStatus.OFFLINE,
                saoOnline = false,
                hasSubscription = false,
                highActivity = false,
                completedAt = now,
            ),
        )
    }

    @Test
    fun `unverifiable presence is polled less aggressively`() {
        assertEquals(
            now.plus(Duration.ofMinutes(2)),
            policy.nextAfterObservation(
                status = PlayerPresenceStatus.UNKNOWN,
                saoOnline = false,
                hasSubscription = false,
                highActivity = false,
                completedAt = now,
            ),
        )
        assertEquals(
            now.plus(Duration.ofMinutes(2)),
            policy.nextAfterObservation(
                status = PlayerPresenceStatus.HIDDEN,
                saoOnline = false,
                hasSubscription = false,
                highActivity = false,
                completedAt = now,
            ),
        )
    }

    @Test
    fun `verified subscription helper uses subscription interval`() {
        assertEquals(
            now.plusSeconds(20),
            policy.nextAfterVerifiedSubscription(now),
        )
    }

    @Test
    fun `failure backoff grows exponentially and is capped`() {
        assertEquals(now.plusSeconds(30), policy.nextAfterFailure(0, now))
        assertEquals(now.plusSeconds(60), policy.nextAfterFailure(1, now))
        assertEquals(now.plus(Duration.ofMinutes(10)), policy.nextAfterFailure(20, now))
    }
}
