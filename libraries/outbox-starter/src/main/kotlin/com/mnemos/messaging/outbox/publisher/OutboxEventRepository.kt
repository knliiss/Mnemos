package com.mnemos.messaging.outbox.publisher

import java.time.Instant
import java.util.UUID

interface OutboxEventRepository {

    fun claimNextPending(
        instanceId: String,
        now: Instant,
        lockedUntil: Instant,
    ): OutboxEventRecord?

    fun markPublished(
        eventId: UUID,
        instanceId: String,
        publishedAt: Instant,
    ): Boolean

    fun markFailed(
        eventId: UUID,
        instanceId: String,
        previousFailureCount: Int,
        failureCount: Int,
        nextAttemptAt: Instant,
        error: String,
    ): Boolean

    fun markDead(
        eventId: UUID,
        instanceId: String,
        previousFailureCount: Int,
        failureCount: Int,
        deadAt: Instant,
        error: String,
    ): Boolean
}