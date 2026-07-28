package com.mnemos.messaging.outbox.publisher

import java.time.Instant
import java.util.UUID

interface OutboxEventRepository {

    fun claimPending(
        instanceId: String,
        now: Instant,
        lockedUntil: Instant,
        batchSize: Int
    ): List<OutboxEventRecord>

    fun markPublished(
        eventId: UUID,
        instanceId: String,
        publishedAt: Instant
    ): Boolean

    fun markFailed(
        eventId: UUID,
        instanceId: String,
        previousAttempts: Int,
        attempts: Int,
        nextAttemptAt: Instant,
        error: String
    ): Boolean

    fun markDead(
        eventId: UUID,
        instanceId: String,
        previousAttempts: Int,
        attempts: Int,
        deadAt: Instant,
        error: String
    ): Boolean
}