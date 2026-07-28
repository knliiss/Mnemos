package com.mnemos.messaging.outbox.cleanup

import com.mnemos.messaging.outbox.config.MnemosOutboxProperties
import com.mnemos.messaging.outbox.support.OutboxClock

class OutboxCleanupService(
    private val repository: OutboxCleanupRepository,
    private val clock: OutboxClock,
    private val properties: MnemosOutboxProperties.Cleanup
) {
    fun cleanup(): Int {
        val now = clock.now()

        val publishedBefore =
            now.minus(properties.publishedRetention)

        val deadBefore =
            now.minus(properties.deadRetention)

        var totalDeleted = 0

        repeat(properties.maxBatchesPerRun) {
            val deleted = repository.deleteExpired(
                publishedBefore = publishedBefore,
                deadBefore = deadBefore,
                batchSize = properties.batchSize
            )

            totalDeleted += deleted

            if (deleted < properties.batchSize) {
                return totalDeleted
            }
        }

        return totalDeleted
    }
}