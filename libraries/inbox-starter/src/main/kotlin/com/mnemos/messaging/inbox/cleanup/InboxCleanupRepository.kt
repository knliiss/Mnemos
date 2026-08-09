package com.mnemos.messaging.inbox.cleanup

import java.time.Instant

fun interface InboxCleanupRepository {
    fun deleteExpired(recordedBefore: Instant, batchSize: Int): Int
}