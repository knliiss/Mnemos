package com.mnemos.messaging.core.event

import com.mnemos.messaging.core.publisher.IntegrationEventRoute
import com.mnemos.messaging.core.transport.EventDestination
import com.mnemos.messaging.core.transport.EventMessage
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import java.time.Instant
import java.util.UUID

class EventContractTest {

    @Test
    fun `create envelope from integration event and route`() {
        val eventId = UUID.randomUUID()
        val aggregateId = UUID.randomUUID()
        val actorUserId = UUID.randomUUID()
        val occurredAt =
            Instant.parse("2026-07-27T14:00:00Z")

        val metadata =
            EventMetadata(
                correlationId = "request-1",
                actorUserId = actorUserId,
            )

        val event =
            IntegrationEvent(
                type = "account.created",
                aggregateId = aggregateId,
                occurredAt = occurredAt,
                payload = TestPayload("Knalis"),
            )

        val route =
            IntegrationEventRoute(
                topic = "mnemos.account.events.v1",
                version = 1,
                aggregateType = "ACCOUNT",
            )

        val envelope =
            EventEnvelope.from(
                eventId = eventId,
                producer = "account-service",
                metadata = metadata,
                event = event,
                route = route,
            )

        assertEquals(eventId, envelope.eventId)
        assertEquals("account.created", envelope.eventType)
        assertEquals(1, envelope.eventVersion)
        assertEquals("account-service", envelope.producer)
        assertEquals("ACCOUNT", envelope.aggregateType)
        assertEquals(aggregateId, envelope.aggregateId)
        assertEquals(occurredAt, envelope.occurredAt)
        assertEquals(metadata, envelope.metadata)
        assertEquals(
            TestPayload("Knalis"),
            envelope.payload,
        )
    }

    @Test
    fun `event type must use lowercase dot notation`() {
        assertThrows(
            IllegalArgumentException::class.java,
        ) {
            IntegrationEvent(
                type = "AccountCreated",
                aggregateId = UUID.randomUUID(),
                occurredAt = Instant.now(),
                payload = TestPayload("Knalis"),
            )
        }
    }

    @Test
    fun `route version must be positive`() {
        assertThrows(
            IllegalArgumentException::class.java,
        ) {
            IntegrationEventRoute(
                topic = "mnemos.account.events.v1",
                version = 0,
                aggregateType = "ACCOUNT",
            )
        }
    }

    @Test
    fun `route aggregate type must use uppercase snake case`() {
        assertThrows(
            IllegalArgumentException::class.java,
        ) {
            IntegrationEventRoute(
                topic = "mnemos.account.events.v1",
                version = 1,
                aggregateType = "Account",
            )
        }
    }

    @Test
    fun `route topic cannot be blank`() {
        assertThrows(
            IllegalArgumentException::class.java,
        ) {
            IntegrationEventRoute(
                topic = " ",
                version = 1,
                aggregateType = "ACCOUNT",
            )
        }
    }

    @Test
    fun `destination requires valid topic and non blank key`() {
        assertThrows(
            IllegalArgumentException::class.java,
        ) {
            EventDestination(
                topic = "mnemos account events",
                key = UUID.randomUUID().toString(),
            )
        }

        assertThrows(
            IllegalArgumentException::class.java,
        ) {
            EventDestination(
                topic = "mnemos.account.events.v1",
                key = " ",
            )
        }
    }

    @Test
    fun `message requires serialized payload`() {
        val destination =
            EventDestination(
                topic = "mnemos.account.events.v1",
                key = UUID.randomUUID().toString(),
            )

        assertThrows(
            IllegalArgumentException::class.java,
        ) {
            EventMessage(
                destination = destination,
                payload = " ",
            )
        }
    }

    private data class TestPayload(
        val nickname: String,
    )
}