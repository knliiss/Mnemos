package com.mnemos.messaging.outbox.config

import org.springframework.boot.context.properties.ConfigurationProperties
import java.time.Duration

@ConfigurationProperties("mnemos.outbox")
data class MnemosOutboxProperties(
    val enabled: Boolean = false,
    val producer: String? = null,
    val requireTransaction: Boolean = true,
    val publisher: Publisher = Publisher(),
    val cleanup: Cleanup = Cleanup()
) {
    data class Publisher(
        val enabled: Boolean = false,
        val initialDelay: Duration = Duration.ofSeconds(1),
        val fixedDelay: Duration = Duration.ofMillis(500),
        val batchSize: Int = 100,
        val leaseDuration: Duration = Duration.ofSeconds(30),
        val sendTimeout: Duration = Duration.ofSeconds(10),
        val maxAttempts: Int = 10,
        val initialBackoff: Duration = Duration.ofSeconds(1),
        val maxBackoff: Duration = Duration.ofMinutes(5)
    ) {
        init {
            require(!initialDelay.isNegative) {
                "Outbox publisher initial delay cannot be negative"
            }

            require(fixedDelay.isPositive()) {
                "Outbox publisher fixed delay must be positive"
            }

            require(batchSize > 0) {
                "Outbox publisher batch size must be positive"
            }

            require(leaseDuration.isPositive()) {
                "Outbox publisher lease duration must be positive"
            }

            require(sendTimeout.isPositive()) {
                "Outbox publisher send timeout must be positive"
            }

            require(maxAttempts > 0) {
                "Outbox publisher max attempts must be positive"
            }

            require(initialBackoff.isPositive()) {
                "Outbox publisher initial backoff must be positive"
            }

            require(maxBackoff >= initialBackoff) {
                "Outbox publisher max backoff must not be smaller than initial backoff"
            }
        }
    }

    data class Cleanup(
        val enabled: Boolean = true,
        val initialDelay: Duration = Duration.ofMinutes(10),
        val fixedDelay: Duration = Duration.ofHours(1),
        val publishedRetention: Duration = Duration.ofDays(7),
        val deadRetention: Duration = Duration.ofDays(30),
        val batchSize: Int = 1_000,
        val maxBatchesPerRun: Int = 10
    ) {
        init {
            require(!initialDelay.isNegative) {
                "Outbox cleanup initial delay cannot be negative"
            }

            require(fixedDelay.isPositive()) {
                "Outbox cleanup fixed delay must be positive"
            }

            require(publishedRetention.isPositive()) {
                "Outbox published retention must be positive"
            }

            require(deadRetention.isPositive()) {
                "Outbox dead retention must be positive"
            }

            require(batchSize > 0) {
                "Outbox cleanup batch size must be positive"
            }

            require(maxBatchesPerRun > 0) {
                "Outbox cleanup max batches per run must be positive"
            }
        }
    }

    private companion object {
        fun Duration.isPositive(): Boolean =
            !isZero && !isNegative
    }
}