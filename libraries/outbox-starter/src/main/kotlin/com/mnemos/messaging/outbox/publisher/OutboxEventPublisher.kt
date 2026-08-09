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
    private val properties: MnemosOutboxProperties.Publisher,
) {

    fun publishBatch(): Int {
        var processed = 0

        repeat(properties.maxEventsPerRun) {
            if (Thread.currentThread().isInterrupted) {
                logger.debug(
                    "Outbox publishing interrupted: processed={}, instanceId={}",
                    processed,
                    instanceId,
                )
                return processed
            }

            val claimedAt = clock.now()

            val event = repository.claimNextPending(
                instanceId = instanceId,
                now = claimedAt,
                lockedUntil = claimedAt.plus(properties.leaseDuration),
            ) ?: return processed

            processed++

            logger.debug(
                "Claimed outbox event: eventId={}, type={}, topic={}, eventKey={}, failureCount={}, instanceId={}",
                event.id,
                event.eventType,
                event.topic,
                event.eventKey,
                event.failureCount,
                instanceId,
            )

            if (!publish(event)) {
                return processed
            }
        }

        return processed
    }

    private fun publish(event: OutboxEventRecord): Boolean {
        logger.debug(
            "Publishing outbox event: eventId={}, type={}, topic={}, eventKey={}, attempt={}",
            event.id,
            event.eventType,
            event.topic,
            event.eventKey,
            event.failureCount + 1,
        )

        val failure = send(event)

        if (failure != null) {
            handleFailure(
                event = event,
                failure = failure,
            )

            return false
        }

        return markPublished(event)
    }

    private fun send(event: OutboxEventRecord): Throwable? =
        try {
            eventTransport
                .publish(event.toEventMessage())
                .toCompletableFuture()
                .get(
                    properties.sendTimeout.toMillis(),
                    TimeUnit.MILLISECONDS,
                )

            logger.debug(
                "Outbox event accepted by transport: eventId={}, type={}, topic={}",
                event.id,
                event.eventType,
                event.topic,
            )

            null
        } catch (exception: InterruptedException) {
            Thread.currentThread().interrupt()

            logger.warn(
                "Outbox event publishing interrupted: eventId={}, type={}, topic={}",
                event.id,
                event.eventType,
                event.topic,
            )

            throw exception
        } catch (exception: Exception) {
            unwrap(exception)
        }

    private fun markPublished(event: OutboxEventRecord): Boolean =
        try {
            val publishedAt = clock.now()

            val updated = repository.markPublished(
                eventId = event.id,
                instanceId = instanceId,
                publishedAt = publishedAt,
            )

            if (!updated) {
                logger.warn(
                    "Could not mark outbox event as published because lock ownership was lost: eventId={}, type={}, topic={}, instanceId={}",
                    event.id,
                    event.eventType,
                    event.topic,
                    instanceId,
                )

                false
            } else {
                logger.debug(
                    "Outbox event published: eventId={}, type={}, topic={}, publishedAt={}",
                    event.id,
                    event.eventType,
                    event.topic,
                    publishedAt,
                )

                true
            }
        } catch (exception: Throwable) {
            logger.error(
                "Transport accepted outbox event but database publish marking failed: eventId={}, type={}, topic={}",
                event.id,
                event.eventType,
                event.topic,
                exception,
            )

            false
        }

    private fun handleFailure(
        event: OutboxEventRecord,
        failure: Throwable,
    ) {
        val failedAt = clock.now()

        val decision = retryPolicy.afterFailure(
            previousFailureCount = event.failureCount,
            now = failedAt,
        )

        val error = formatError(failure)

        try {
            val updated =
                if (decision.dead) {
                    repository.markDead(
                        eventId = event.id,
                        instanceId = instanceId,
                        previousFailureCount = event.failureCount,
                        failureCount = decision.failureCount,
                        deadAt = failedAt,
                        error = error,
                    )
                } else {
                    repository.markFailed(
                        eventId = event.id,
                        instanceId = instanceId,
                        previousFailureCount = event.failureCount,
                        failureCount = decision.failureCount,
                        nextAttemptAt = requireNotNull(decision.nextAttemptAt),
                        error = error,
                    )
                }

            if (!updated) {
                logger.warn(
                    "Could not update failed outbox event because lock ownership was lost: eventId={}, type={}, topic={}, instanceId={}",
                    event.id,
                    event.eventType,
                    event.topic,
                    instanceId,
                )
                return
            }

            if (decision.dead) {
                logger.error(
                    "Outbox event moved to DEAD: eventId={}, type={}, topic={}, failureCount={}, error={}",
                    event.id,
                    event.eventType,
                    event.topic,
                    decision.failureCount,
                    error,
                )
            } else {
                logger.warn(
                    "Outbox event publish failed, retry scheduled: eventId={}, type={}, topic={}, failureCount={}, nextAttemptAt={}, error={}",
                    event.id,
                    event.eventType,
                    event.topic,
                    decision.failureCount,
                    decision.nextAttemptAt,
                    error,
                )
            }
        } catch (exception: Throwable) {
            logger.error(
                "Could not persist outbox publishing failure: eventId={}, type={}, topic={}",
                event.id,
                event.eventType,
                event.topic,
                exception,
            )
        }
    }

    private fun formatError(failure: Throwable): String {
        val type =
            failure::class.qualifiedName
                ?: failure::class.simpleName
                ?: "UnknownException"

        return "$type: ${failure.message.orEmpty()}"
            .take(MAX_ERROR_LENGTH)
    }

    private tailrec fun unwrap(exception: Throwable): Throwable =
        when (exception) {
            is ExecutionException,
            is CompletionException ->
                exception.cause?.let(::unwrap) ?: exception

            else -> exception
        }

    private companion object {
        const val MAX_ERROR_LENGTH = 4_000

        val logger = LoggerFactory.getLogger(OutboxEventPublisher::class.java)
    }
}