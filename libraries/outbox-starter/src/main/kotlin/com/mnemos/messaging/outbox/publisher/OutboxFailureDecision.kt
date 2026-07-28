package com.mnemos.messaging.outbox.publisher

import java.time.Duration
import java.time.Instant

data class OutboxFailureDecision(
    val attempts: Int,
    val dead: Boolean,
    val nextAttemptAt: Instant?
)

class OutboxRetryPolicy(
    private val maxAttempts: Int,
    private val initialBackoff: Duration,
    private val maxBackoff: Duration
) {
    init {
        require(maxAttempts > 0)
        require(!initialBackoff.isZero && !initialBackoff.isNegative)
        require(maxBackoff >= initialBackoff)
    }

    fun afterFailure(
        previousAttempts: Int,
        now: Instant
    ): OutboxFailureDecision {
        require(previousAttempts >= 0)

        val attempts = previousAttempts + 1

        if (attempts >= maxAttempts) {
            return OutboxFailureDecision(
                attempts = attempts,
                dead = true,
                nextAttemptAt = null
            )
        }

        return OutboxFailureDecision(
            attempts = attempts,
            dead = false,
            nextAttemptAt = now.plus(
                calculateBackoff(attempts)
            )
        )
    }

    private fun calculateBackoff(attempts: Int): Duration {
        var delay = initialBackoff

        repeat(attempts - 1) {
            delay = try {
                minOf(
                    delay.multipliedBy(2),
                    maxBackoff
                )
            } catch (_: ArithmeticException) {
                maxBackoff
            }
        }

        return delay
    }
}