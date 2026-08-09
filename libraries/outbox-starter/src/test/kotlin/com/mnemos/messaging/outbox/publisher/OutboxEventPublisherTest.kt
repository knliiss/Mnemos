package com.mnemos.messaging.outbox.publisher

import com.mnemos.messaging.core.transport.EventTransport
import com.mnemos.messaging.outbox.config.MnemosOutboxProperties
import com.mnemos.messaging.outbox.support.OutboxClock
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.time.Duration
import java.time.Instant
import java.util.UUID
import java.util.concurrent.CompletableFuture

class OutboxEventPublisherTest {

    private val repository =
        mock<OutboxEventRepository>()

    private val eventTransport =
        mock<EventTransport>()

    private val now =
        Instant.parse("2026-08-08T18:00:00Z")

    private val properties =
        MnemosOutboxProperties.Publisher(
            enabled = true,
            maxEventsPerRun = 10,
            leaseDuration = Duration.ofSeconds(30),
            sendTimeout = Duration.ofSeconds(10),
            maxAttempts = 3,
            initialBackoff = Duration.ofSeconds(1),
            maxBackoff = Duration.ofSeconds(10),
        )

    private val retryPolicy =
        OutboxRetryPolicy(
            maxAttempts = properties.maxAttempts,
            initialBackoff = properties.initialBackoff,
            maxBackoff = properties.maxBackoff,
        )

    private val publisher =
        OutboxEventPublisher(
            repository = repository,
            eventTransport = eventTransport,
            retryPolicy = retryPolicy,
            clock = OutboxClock { now },
            instanceId = INSTANCE_ID,
            properties = properties,
        )

    @Test
    fun `empty outbox publishes nothing`() {
        whenever(
            repository.claimNextPending(
                instanceId = INSTANCE_ID,
                now = now,
                lockedUntil = now.plusSeconds(30),
            ),
        ).thenReturn(null)

        val processed = publisher.publishBatch()

        assertEquals(
            0,
            processed,
        )

        verify(
            eventTransport,
            never(),
        ).publish(any())
    }

    @Test
    fun `successful event is published and marked published`() {
        val event = event()

        whenever(
            repository.claimNextPending(
                instanceId = INSTANCE_ID,
                now = now,
                lockedUntil = now.plusSeconds(30),
            ),
        ).thenReturn(
            event,
            null,
        )

        whenever(
            eventTransport.publish(any()),
        ).thenReturn(
            CompletableFuture.completedFuture(Unit),
        )

        whenever(
            repository.markPublished(
                eventId = event.id,
                instanceId = INSTANCE_ID,
                publishedAt = now,
            ),
        ).thenReturn(true)

        val processed = publisher.publishBatch()

        assertEquals(
            1,
            processed,
        )

        verify(eventTransport)
            .publish(
                event.toEventMessage(),
            )

        verify(repository)
            .markPublished(
                eventId = event.id,
                instanceId = INSTANCE_ID,
                publishedAt = now,
            )

        verify(
            repository,
            never(),
        ).markFailed(
            eventId = any(),
            instanceId = any(),
            previousFailureCount = any(),
            failureCount = any(),
            nextAttemptAt = any(),
            error = any(),
        )
    }

    @Test
    fun `publisher claims each event immediately before publishing it`() {
        val first = event()
        val second = event()

        whenever(
            repository.claimNextPending(
                instanceId = INSTANCE_ID,
                now = now,
                lockedUntil = now.plusSeconds(30),
            ),
        ).thenReturn(
            first,
            second,
            null,
        )

        whenever(
            eventTransport.publish(any()),
        ).thenReturn(
            CompletableFuture.completedFuture(Unit),
        )

        whenever(
            repository.markPublished(
                eventId = any(),
                instanceId = eq(INSTANCE_ID),
                publishedAt = eq(now),
            ),
        ).thenReturn(true)

        val processed = publisher.publishBatch()

        assertEquals(
            2,
            processed,
        )

        verify(
            repository,
            times(3),
        ).claimNextPending(
            instanceId = INSTANCE_ID,
            now = now,
            lockedUntil = now.plusSeconds(30),
        )

        verify(eventTransport)
            .publish(first.toEventMessage())

        verify(eventTransport)
            .publish(second.toEventMessage())
    }

    @Test
    fun `failed transport schedules retry`() {
        val event =
            event(
                failureCount = 0,
            )

        whenever(
            repository.claimNextPending(
                instanceId = INSTANCE_ID,
                now = now,
                lockedUntil = now.plusSeconds(30),
            ),
        ).thenReturn(event)

        whenever(
            eventTransport.publish(any()),
        ).thenReturn(
            CompletableFuture.failedFuture(
                IllegalStateException(
                    "Kafka unavailable",
                ),
            ),
        )

        whenever(
            repository.markFailed(
                eventId = event.id,
                instanceId = INSTANCE_ID,
                previousFailureCount = 0,
                failureCount = 1,
                nextAttemptAt = now.plusSeconds(1),
                error =
                    "java.lang.IllegalStateException: Kafka unavailable",
            ),
        ).thenReturn(true)

        val processed = publisher.publishBatch()

        assertEquals(
            1,
            processed,
        )

        verify(repository)
            .markFailed(
                eventId = event.id,
                instanceId = INSTANCE_ID,
                previousFailureCount = 0,
                failureCount = 1,
                nextAttemptAt = now.plusSeconds(1),
                error =
                    "java.lang.IllegalStateException: Kafka unavailable",
            )

        verify(
            repository,
            never(),
        ).markPublished(
            eventId = any(),
            instanceId = any(),
            publishedAt = any(),
        )
    }

    @Test
    fun `failed transport stops current run`() {
        val first = event()
        val second = event()

        whenever(
            repository.claimNextPending(
                instanceId = INSTANCE_ID,
                now = now,
                lockedUntil = now.plusSeconds(30),
            ),
        ).thenReturn(
            first,
            second,
        )

        whenever(
            eventTransport.publish(any()),
        ).thenReturn(
            CompletableFuture.failedFuture(
                IllegalStateException(
                    "Kafka unavailable",
                ),
            ),
        )

        whenever(
            repository.markFailed(
                eventId = first.id,
                instanceId = INSTANCE_ID,
                previousFailureCount = 0,
                failureCount = 1,
                nextAttemptAt = now.plusSeconds(1),
                error =
                    "java.lang.IllegalStateException: Kafka unavailable",
            ),
        ).thenReturn(true)

        val processed = publisher.publishBatch()

        assertEquals(
            1,
            processed,
        )

        verify(
            eventTransport,
            times(1),
        ).publish(any())

        verify(
            repository,
            times(1),
        ).claimNextPending(
            instanceId = INSTANCE_ID,
            now = now,
            lockedUntil = now.plusSeconds(30),
        )
    }

    @Test
    fun `event becomes dead after final failure`() {
        val event =
            event(
                failureCount = 2,
            )

        whenever(
            repository.claimNextPending(
                instanceId = INSTANCE_ID,
                now = now,
                lockedUntil = now.plusSeconds(30),
            ),
        ).thenReturn(event)

        whenever(
            eventTransport.publish(any()),
        ).thenReturn(
            CompletableFuture.failedFuture(
                IllegalStateException(
                    "Kafka unavailable",
                ),
            ),
        )

        whenever(
            repository.markDead(
                eventId = event.id,
                instanceId = INSTANCE_ID,
                previousFailureCount = 2,
                failureCount = 3,
                deadAt = now,
                error =
                    "java.lang.IllegalStateException: Kafka unavailable",
            ),
        ).thenReturn(true)

        val processed = publisher.publishBatch()

        assertEquals(
            1,
            processed,
        )

        verify(repository)
            .markDead(
                eventId = event.id,
                instanceId = INSTANCE_ID,
                previousFailureCount = 2,
                failureCount = 3,
                deadAt = now,
                error =
                    "java.lang.IllegalStateException: Kafka unavailable",
            )

        verify(
            repository,
            never(),
        ).markFailed(
            eventId = any(),
            instanceId = any(),
            previousFailureCount = any(),
            failureCount = any(),
            nextAttemptAt = any(),
            error = any(),
        )
    }

    @Test
    fun `lost ownership after successful publish stops current run`() {
        val event = event()

        whenever(
            repository.claimNextPending(
                instanceId = INSTANCE_ID,
                now = now,
                lockedUntil = now.plusSeconds(30),
            ),
        ).thenReturn(event)

        whenever(
            eventTransport.publish(any()),
        ).thenReturn(
            CompletableFuture.completedFuture(Unit),
        )

        whenever(
            repository.markPublished(
                eventId = event.id,
                instanceId = INSTANCE_ID,
                publishedAt = now,
            ),
        ).thenReturn(false)

        val processed = publisher.publishBatch()

        assertEquals(
            1,
            processed,
        )

        verify(eventTransport)
            .publish(event.toEventMessage())

        verify(
            repository,
            never(),
        ).markFailed(
            eventId = any(),
            instanceId = any(),
            previousFailureCount = any(),
            failureCount = any(),
            nextAttemptAt = any(),
            error = any(),
        )
    }

    private fun event(
        failureCount: Int = 0,
    ): OutboxEventRecord =
        OutboxEventRecord(
            id = UUID.randomUUID(),
            topic = "mnemos.account.events.v1",
            eventKey = UUID.randomUUID().toString(),
            eventType = "account.created",
            eventVersion = 1,
            producer = "account-service",
            aggregateType = "ACCOUNT",
            aggregateId = UUID.randomUUID(),
            correlationId = null,
            causationId = null,
            actorUserId = null,
            envelope =
                """{"eventType":"account.created"}""",
            occurredAt = now.minusSeconds(1),
            failureCount = failureCount,
        )

    private companion object {
        const val INSTANCE_ID =
            "account-service:test-instance"
    }
}