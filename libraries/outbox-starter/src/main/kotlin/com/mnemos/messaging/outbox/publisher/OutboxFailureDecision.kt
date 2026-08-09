package com.mnemos.messaging.outbox.publisher

import java.time.Duration
import java.time.Instant

data class OutboxFailureDecision(
    val failureCount: Int,
    val dead: Boolean,
    val nextAttemptAt: Instant?,
)

class OutboxRetryPolicy(
    private val maxAttempts: Int,
    private val initialBackoff: Duration,
    private val maxBackoff: Duration,
) {

    init {
        require(maxAttempts > 0)
        require(!initialBackoff.isZero && !initialBackoff.isNegative)
        require(maxBackoff >= initialBackoff)
    }

    fun afterFailure(
        previousFailureCount: Int,
        now: Instant,
    ): OutboxFailureDecision {
        require(previousFailureCount >= 0)

        val failureCount = previousFailureCount + 1

        if (failureCount >= maxAttempts) {
            return OutboxFailureDecision(
                failureCount = failureCount,
                dead = true,
                nextAttemptAt = null,
            )
        }

        return OutboxFailureDecision(
            failureCount = failureCount,
            dead = false,
            nextAttemptAt = now.plus(
                calculateBackoff(failureCount),
            ),
        )
    }

    private fun calculateBackoff(
        failureCount: Int,
    ): Duration {
        var delay = initialBackoff

        repeat(failureCount - 1) {
            delay =
                try {
                    minOf(
                        delay.multipliedBy(2),
                        maxBackoff,
                    )
                } catch (_: ArithmeticException) {
                    maxBackoff
                }
        }

        return delay
    }
}