package com.mnemos.messaging.outbox.cleanup

import java.time.Instant

fun interface OutboxCleanupRepository {

    fun deleteExpired(
        publishedBefore: Instant,
        deadBefore: Instant,
        batchSize: Int
    ): Int
}