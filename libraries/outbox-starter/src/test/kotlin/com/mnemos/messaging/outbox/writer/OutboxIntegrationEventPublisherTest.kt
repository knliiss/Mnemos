package com.mnemos.messaging.outbox.writer

import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventRoute
import com.mnemos.messaging.core.publisher.IntegrationEventRouteResolver
import com.mnemos.messaging.outbox.metadata.EventMetadataProvider
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class OutboxIntegrationEventPublisherTest {

    @Test
    fun `publish resolves route and merges metadata`() {
        val eventId =
            UUID.randomUUID()

        val aggregateId =
            UUID.randomUUID()

        val causationId =
            UUID.randomUUID()

        val actorUserId =
            UUID.randomUUID()

        val route =
            IntegrationEventRoute(
                topic = "mnemos.account.events.v1",
                version = 1,
                aggregateType = "ACCOUNT",
            )

        val event =
            IntegrationEvent(
                type = "account.created",
                aggregateId = aggregateId,
                occurredAt =
                    Instant.parse(
                        "2026-08-08T18:00:00Z",
                    ),
                payload =
                    TestPayload(
                        nickname = "Knalis",
                    ),
            )

        val writer =
            RecordingOutboxEventWriter(
                eventId,
            )

        val publisher =
            OutboxIntegrationEventPublisher(
                outboxEventWriter = writer,
                routeResolver =
                    IntegrationEventRouteResolver {
                        route
                    },
                metadataProvider =
                    EventMetadataProvider {
                        EventMetadata(
                            correlationId =
                                "request-1",
                            causationId =
                                causationId,
                        )
                    },
            )

        val result =
            publisher.publish(
                event = event,
                metadata =
                    EventMetadata(
                        actorUserId =
                            actorUserId,
                    ),
            )

        assertEquals(
            eventId,
            result,
        )

        assertEquals(
            route,
            writer.written.route,
        )

        assertEquals(
            event,
            writer.written.event,
        )

        assertEquals(
            EventMetadata(
                correlationId = "request-1",
                causationId = causationId,
                actorUserId = actorUserId,
            ),
            writer.written.metadata,
        )
    }

    @Test
    fun `explicit metadata overrides current metadata`() {
        val explicitCausationId =
            UUID.randomUUID()

        val writer =
            RecordingOutboxEventWriter(
                UUID.randomUUID(),
            )

        val publisher =
            OutboxIntegrationEventPublisher(
                outboxEventWriter = writer,
                routeResolver =
                    IntegrationEventRouteResolver {
                        IntegrationEventRoute(
                            topic =
                                "mnemos.account.events.v1",
                            version = 1,
                            aggregateType = "ACCOUNT",
                        )
                    },
                metadataProvider =
                    EventMetadataProvider {
                        EventMetadata(
                            correlationId =
                                "current-correlation",
                            causationId =
                                UUID.randomUUID(),
                        )
                    },
            )

        publisher.publish(
            event =
                IntegrationEvent(
                    type = "account.created",
                    aggregateId =
                        UUID.randomUUID(),
                    occurredAt =
                        Instant.now(),
                    payload =
                        TestPayload("Knalis"),
                ),
            metadata =
                EventMetadata(
                    correlationId =
                        "explicit-correlation",
                    causationId =
                        explicitCausationId,
                ),
        )

        assertEquals(
            "explicit-correlation",
            writer.written
                .metadata
                .correlationId,
        )

        assertEquals(
            explicitCausationId,
            writer.written
                .metadata
                .causationId,
        )
    }

    private class RecordingOutboxEventWriter(
        private val eventId: UUID,
    ) : OutboxEventWriter {

        lateinit var written: WrittenEvent

        override fun <T : Any> append(
            route: IntegrationEventRoute,
            event: IntegrationEvent<T>,
            metadata: EventMetadata,
        ): UUID {
            written =
                WrittenEvent(
                    route = route,
                    event = event,
                    metadata = metadata,
                )

            return eventId
        }
    }

    private data class WrittenEvent(
        val route: IntegrationEventRoute,
        val event: IntegrationEvent<*>,
        val metadata: EventMetadata,
    )

    private data class TestPayload(
        val nickname: String,
    )
}