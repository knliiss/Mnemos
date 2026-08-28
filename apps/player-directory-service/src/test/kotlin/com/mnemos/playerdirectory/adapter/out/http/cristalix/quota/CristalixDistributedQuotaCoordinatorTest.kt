package com.mnemos.playerdirectory.adapter.out.http.cristalix.quota

import com.mnemos.playerdirectory.adapter.out.http.cristalix.CristalixApiProperties
import com.mnemos.playerdirectory.adapter.out.http.cristalix.CristalixCredentialLane
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Duration
import java.time.Instant

class CristalixDistributedQuotaCoordinatorTest {
    private val repository = mock<CristalixQuotaStateJpaRepository>()
    private val coordinator = CristalixDistributedQuotaCoordinator(repository)

    @Test
    fun `refills and consumes the shared token bucket`() {
        val now = Instant.parse("2026-08-28T00:00:00Z")
        val state =
            CristalixQuotaStateEntity(
                lane = CristalixCredentialLane.INTERACTIVE,
                availableTokens = 0.0,
                lastRefillAt = now.minusSeconds(30),
                cooldownUntil = null,
            )

        whenever(repository.findByLaneForUpdate(CristalixCredentialLane.INTERACTIVE)).thenReturn(state)
        whenever(repository.save(any())).thenAnswer { invocation -> invocation.arguments[0] }

        val decision =
            coordinator.tryAcquire(
                lane = CristalixCredentialLane.INTERACTIVE,
                rateLimit = rateLimit(permits = 2),
                now = now,
            )

        assertEquals(CristalixQuotaDecisionStatus.PERMITTED, decision.status)
        assertEquals(0.0, state.availableTokens, 0.000001)
        assertEquals(now, state.lastRefillAt)
        verify(repository).insertIfAbsent(
            lane = CristalixCredentialLane.INTERACTIVE.name,
            capacity = 2.0,
            now = now,
        )
    }

    @Test
    fun `returns retry delay when shared quota is exhausted`() {
        val now = Instant.parse("2026-08-28T00:00:00Z")
        val state =
            CristalixQuotaStateEntity(
                lane = CristalixCredentialLane.MONITORING,
                availableTokens = 0.0,
                lastRefillAt = now,
                cooldownUntil = null,
            )

        whenever(repository.findByLaneForUpdate(CristalixCredentialLane.MONITORING)).thenReturn(state)
        whenever(repository.save(any())).thenAnswer { invocation -> invocation.arguments[0] }

        val decision =
            coordinator.tryAcquire(
                lane = CristalixCredentialLane.MONITORING,
                rateLimit = rateLimit(permits = 2),
                now = now,
            )

        assertEquals(CristalixQuotaDecisionStatus.QUOTA_EXHAUSTED, decision.status)
        assertEquals(Duration.ofSeconds(30), decision.retryAfter)
    }

    @Test
    fun `shares upstream cooldown across replicas`() {
        val now = Instant.parse("2026-08-28T00:00:00Z")
        val cooldownUntil = now.plusSeconds(45)
        val state =
            CristalixQuotaStateEntity(
                lane = CristalixCredentialLane.RESERVE,
                availableTokens = 10.0,
                lastRefillAt = now,
                cooldownUntil = cooldownUntil,
            )

        whenever(repository.findByLaneForUpdate(CristalixCredentialLane.RESERVE)).thenReturn(state)

        val decision =
            coordinator.tryAcquire(
                lane = CristalixCredentialLane.RESERVE,
                rateLimit = rateLimit(permits = 240),
                now = now,
            )

        assertEquals(CristalixQuotaDecisionStatus.COOLING_DOWN, decision.status)
        assertEquals(Duration.ofSeconds(45), decision.retryAfter)
    }

    @Test
    fun `never shortens an existing upstream cooldown`() {
        val now = Instant.parse("2026-08-28T00:00:00Z")
        val existingCooldown = now.plusSeconds(60)
        val state =
            CristalixQuotaStateEntity(
                lane = CristalixCredentialLane.INTERACTIVE,
                availableTokens = 10.0,
                lastRefillAt = now,
                cooldownUntil = existingCooldown,
            )

        whenever(repository.findByLaneForUpdate(CristalixCredentialLane.INTERACTIVE)).thenReturn(state)

        coordinator.applyCooldown(
            lane = CristalixCredentialLane.INTERACTIVE,
            rateLimit = rateLimit(permits = 280),
            cooldownUntil = now.plusSeconds(30),
            now = now,
        )

        assertEquals(existingCooldown, state.cooldownUntil)
        assertTrue(state.availableTokens >= 0.0)
    }

    private fun rateLimit(permits: Int): CristalixApiProperties.RateLimit =
        CristalixApiProperties.RateLimit(
            permits = permits,
            refreshPeriod = Duration.ofMinutes(1),
            timeout = Duration.ZERO,
        )
}
