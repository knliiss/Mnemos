package com.mnemos.messaging.outbox.publisher

import com.mnemos.messaging.core.transport.EventTransport
import com.mnemos.messaging.outbox.config.MnemosOutboxProperties
import com.mnemos.messaging.outbox.support.OutboxClock
import org.slf4j.LoggerFactory
import java.util.concurrent.CompletionException
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit

class OutboxEventPublisher(
    private val repository: OutboxEventRepository,
    private val eventTransport: EventTransport,
    private val retryPolicy: OutboxRetryPolicy,
    private val clock: OutboxClock,
    private val instanceId: String,
    private val properties: MnemosOutboxProperties.Publisher
) {
    fun publishBatch(): Int {
        val now = clock.now()

        val events = repository.claimPending(
            instanceId = instanceId,
            now = now,
            lockedUntil = now.plus(properties.leaseDuration),
            batchSize = properties.batchSize
        )

        events.forEach(::publish)

        return events.size
    }

    private fun publish(event: OutboxEventRecord) {
        val failure = send(event)

        if (failure != null) {
            handleFailure(event, failure)
            return
        }

        markPublished(event)
    }

    private fun send(event: OutboxEventRecord): Throwable? =
        try {
            eventTransport
                .publish(event.toEventMessage())
                .toCompletableFuture()
                .get(
                    properties.sendTimeout.toMillis(),
                    TimeUnit.MILLISECONDS
                )

            null
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()
            exception
        } catch (exception: Throwable) {
            unwrap(exception)
        }

    private fun markPublished(event: OutboxEventRecord) {
        try {
            val updated = repository.markPublished(
                eventId = event.id,
                instanceId = instanceId,
                publishedAt = clock.now()
            )

            if (!updated) {
                logger.warn(
                    "Could not mark outbox event as published because lock ownership was lost: eventId={}",
                    event.id
                )
            }
        } catch (exception: Throwable) {
            /*
             * Kafka already accepted the event.
             * Do not call markFailed here, otherwise we would pretend
             * that the Kafka send itself failed.
             *
             * The lease will expire and the event may be published
             * again. Consumers therefore must be idempotent by eventId.
             */
            logger.error(
                "Kafka accepted outbox event but database publish marking failed: eventId={}",
                event.id,
                exception
            )
        }
    }

    private fun handleFailure(
        event: OutboxEventRecord,
        failure: Throwable
    ) {
        val failedAt = clock.now()

        val decision = retryPolicy.afterFailure(
            previousAttempts = event.attempts,
            now = failedAt
        )

        val error = formatError(failure)

        try {
            val updated = if (decision.dead) {
                repository.markDead(
                    eventId = event.id,
                    instanceId = instanceId,
                    previousAttempts = event.attempts,
                    attempts = decision.attempts,
                    deadAt = failedAt,
                    error = error
                )
            } else {
                repository.markFailed(
                    eventId = event.id,
                    instanceId = instanceId,
                    previousAttempts = event.attempts,
                    attempts = decision.attempts,
                    nextAttemptAt =
                        requireNotNull(decision.nextAttemptAt),
                    error = error
                )
            }

            if (!updated) {
                logger.warn(
                    "Could not update failed outbox event because lock ownership was lost: eventId={}",
                    event.id
                )
            }
        } catch (exception: Throwable) {
            logger.error(
                "Could not persist outbox publishing failure: eventId={}",
                event.id,
                exception
            )
        }
    }

    private fun formatError(failure: Throwable): String {
        val type = failure::class.qualifiedName
            ?: failure::class.simpleName
            ?: "UnknownException"

        return "$type: ${failure.message.orEmpty()}"
            .take(MAX_ERROR_LENGTH)
    }

    private tailrec fun unwrap(
        exception: Throwable
    ): Throwable =
        when (exception) {
            is ExecutionException,
            is CompletionException ->
                exception.cause?.let(::unwrap) ?: exception

            else -> exception
        }

    private companion object {
        const val MAX_ERROR_LENGTH = 4_000

        val logger =
            LoggerFactory.getLogger(OutboxEventPublisher::class.java)
    }
}