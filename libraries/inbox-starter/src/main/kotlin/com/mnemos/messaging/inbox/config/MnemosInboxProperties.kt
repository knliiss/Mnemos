package com.mnemos.messaging.inbox.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.scheduling.support.CronExpression
import java.time.Duration
import java.time.ZoneId

@ConfigurationProperties("mnemos.inbox")
data class MnemosInboxProperties(
    val enabled: Boolean = false,
    val cleanup: Cleanup = Cleanup(),
) {
    data class Cleanup(
        val enabled: Boolean = true,
        val cron: String = "0 0 4 * * SUN",
        val zone: String = "UTC",
        val retention: Duration = Duration.ofDays(30),
        val batchSize: Int = 1_000,
        val maxBatchesPerRun: Int = 100,
    ) {
        init {
            CronExpression.parse(cron)
            ZoneId.of(zone)

            require(retention.isPositive()) {
                "Inbox cleanup retention must be positive"
            }
            require(batchSize > 0) {
                "Inbox cleanup batch size must be positive"
            }
            require(maxBatchesPerRun > 0) {
                "Inbox cleanup max batches per run must be positive"
            }
        }
    }

    private companion object {
        fun Duration.isPositive(): Boolean = !isZero && !isNegative
    }
}