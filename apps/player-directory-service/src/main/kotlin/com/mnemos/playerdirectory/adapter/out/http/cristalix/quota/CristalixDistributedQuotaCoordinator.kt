package com.mnemos.playerdirectory.adapter.out.http.cristalix.quota

import com.mnemos.playerdirectory.adapter.out.http.cristalix.CristalixApiProperties
import com.mnemos.playerdirectory.adapter.out.http.cristalix.CristalixCredentialLane
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.min

@Component
class CristalixDistributedQuotaCoordinator(
    private val repository: CristalixQuotaStateJpaRepository,
) {
    @Transactional
    fun tryAcquire(
        lane: CristalixCredentialLane,
        rateLimit: CristalixApiProperties.RateLimit,
        now: Instant,
    ): CristalixQuotaDecision {
        validate(rateLimit)

        val capacity = rateLimit.permits.toDouble()

        repository.insertIfAbsent(
            lane = lane.name,
            capacity = capacity,
            now = now,
        )

        val state =
            checkNotNull(repository.findByLaneForUpdate(lane)) {
                "Cristalix quota state is missing after initialization: $lane"
            }

        val cooldownUntil = state.cooldownUntil

        if (cooldownUntil != null && cooldownUntil.isAfter(now)) {
            return CristalixQuotaDecision.coolingDown(
                retryAfter = Duration.between(now, cooldownUntil),
            )
        }

        if (cooldownUntil != null) {
            state.cooldownUntil = null
        }

        val refreshMillis = rateLimit.refreshPeriod.toMillis()
        val elapsedMillis = max(0L, Duration.between(state.lastRefillAt, now).toMillis())
        val refillPerMillisecond = capacity / refreshMillis.toDouble()
        val available =
            min(
                capacity,
                state.availableTokens + elapsedMillis.toDouble() * refillPerMillisecond,
            )

        state.lastRefillAt = now

        if (available >= REQUIRED_TOKEN_COUNT) {
            state.availableTokens = available - REQUIRED_TOKEN_COUNT
            repository.save(state)

            return CristalixQuotaDecision.permitted()
        }

        state.availableTokens = available
        repository.save(state)

        val missingTokens = REQUIRED_TOKEN_COUNT - available
        val waitMillis =
            max(
                1L,
                ceil(missingTokens / refillPerMillisecond).toLong(),
            )

        return CristalixQuotaDecision.quotaExhausted(
            retryAfter = Duration.ofMillis(waitMillis),
        )
    }

    @Transactional
    fun applyCooldown(
        lane: CristalixCredentialLane,
        rateLimit: CristalixApiProperties.RateLimit,
        cooldownUntil: Instant,
        now: Instant,
    ) {
        validate(rateLimit)

        repository.insertIfAbsent(
            lane = lane.name,
            capacity = rateLimit.permits.toDouble(),
            now = now,
        )

        val state =
            checkNotNull(repository.findByLaneForUpdate(lane)) {
                "Cristalix quota state is missing after initialization: $lane"
            }

        val current = state.cooldownUntil

        if (current == null || cooldownUntil.isAfter(current)) {
            state.cooldownUntil = cooldownUntil
            repository.save(state)
        }
    }

    private fun validate(rateLimit: CristalixApiProperties.RateLimit) {
        require(rateLimit.permits > 0) {
            "Cristalix rate limit permits must be positive"
        }
        require(rateLimit.refreshPeriod.isPositive) {
            "Cristalix rate limit refresh period must be positive"
        }
        require(!rateLimit.timeout.isNegative) {
            "Cristalix rate limit timeout cannot be negative"
        }
        require(rateLimit.refreshPeriod.toMillis() > 0) {
            "Cristalix rate limit refresh period must be at least one millisecond"
        }
    }

    private companion object {
        const val REQUIRED_TOKEN_COUNT = 1.0
    }
}

data class CristalixQuotaDecision(
    val status: CristalixQuotaDecisionStatus,
    val retryAfter: Duration?,
) {
    companion object {
        fun permitted(): CristalixQuotaDecision =
            CristalixQuotaDecision(
                status = CristalixQuotaDecisionStatus.PERMITTED,
                retryAfter = null,
            )

        fun quotaExhausted(retryAfter: Duration): CristalixQuotaDecision =
            CristalixQuotaDecision(
                status = CristalixQuotaDecisionStatus.QUOTA_EXHAUSTED,
                retryAfter = retryAfter,
            )

        fun coolingDown(retryAfter: Duration): CristalixQuotaDecision =
            CristalixQuotaDecision(
                status = CristalixQuotaDecisionStatus.COOLING_DOWN,
                retryAfter = retryAfter,
            )
    }
}

enum class CristalixQuotaDecisionStatus {
    PERMITTED,
    QUOTA_EXHAUSTED,
    COOLING_DOWN,
}
