package com.mnemos.messaging.outbox.writer

import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventPublisher
import com.mnemos.messaging.core.publisher.IntegrationEventRouteResolver
import com.mnemos.messaging.outbox.metadata.EventMetadataProvider
import org.slf4j.LoggerFactory
import java.util.UUID

class OutboxIntegrationEventPublisher(
    private val outboxEventWriter: OutboxEventWriter,
    private val routeResolver: IntegrationEventRouteResolver,
    private val metadataProvider: EventMetadataProvider,
) : IntegrationEventPublisher {

    override fun <T : Any> publish(
        event: IntegrationEvent<T>,
        metadata: EventMetadata,
    ): UUID {
        logger.debug(
            "Publishing integration event to outbox: type={}, aggregateId={}",
            event.type,
            event.aggregateId,
        )

        val route = routeResolver.resolve(event)

        logger.debug(
            "Resolved integration event route: type={}, aggregateId={}, topic={}, version={}, aggregateType={}",
            event.type,
            event.aggregateId,
            route.topic,
            route.version,
            route.aggregateType,
        )

        val currentMetadata = metadataProvider.current()

        val resolvedMetadata = EventMetadata(
            correlationId = metadata.correlationId ?: currentMetadata.correlationId,
            causationId = metadata.causationId ?: currentMetadata.causationId,
            actorUserId = metadata.actorUserId ?: currentMetadata.actorUserId,
        )

        return try {
            val messageId = outboxEventWriter.append(
                route = route,
                event = event,
                metadata = resolvedMetadata,
            )

            logger.debug(
                "Integration event appended to outbox: messageId={}, type={}, aggregateId={}, topic={}",
                messageId,
                event.type,
                event.aggregateId,
                route.topic,
            )

            messageId
        } catch (exception: Throwable) {
            logger.error(
                "Failed to append integration event to outbox: type={}, aggregateId={}, topic={}, aggregateType={}",
                event.type,
                event.aggregateId,
                route.topic,
                route.aggregateType,
                exception,
            )

            throw exception
        }
    }

    private companion object {
        val logger = LoggerFactory.getLogger(OutboxIntegrationEventPublisher::class.java)
    }
}