package com.mnemos.messaging.inbox.cleanup

import com.mnemos.messaging.inbox.InboxClock
import com.mnemos.messaging.inbox.config.MnemosInboxProperties

class InboxCleanupService(
    private val repository: InboxCleanupRepository,
    private val clock: InboxClock,
    private val properties: MnemosInboxProperties.Cleanup,
) {
    fun cleanup(): Int {
        val recordedBefore = clock.now().minus(properties.retention)
        var totalDeleted = 0

        repeat(properties.maxBatchesPerRun) {
            val deleted = repository.deleteExpired(
                recordedBefore = recordedBefore,
                batchSize = properties.batchSize,
            )

            totalDeleted += deleted

            if (deleted < properties.batchSize) {
                return totalDeleted
            }
        }

        return totalDeleted
    }
}