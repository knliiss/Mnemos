package com.mnemos.messaging.outbox.writer

import com.mnemos.messaging.core.event.EventEnvelope
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.transport.EventDestination
import com.mnemos.messaging.outbox.metadata.EventMetadataProvider
import com.mnemos.messaging.outbox.serialization.EventEnvelopeSerializer
import com.mnemos.messaging.outbox.support.OutboxClock
import com.mnemos.messaging.outbox.support.OutboxEventIdGenerator
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.whenever
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.jdbc.core.namedparam.SqlParameterSource
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

class JdbcOutboxEventWriterTest {

    private val jdbcOperations =
        mock<NamedParameterJdbcOperations>()

    @AfterEach
    fun clearTransactionState() {
        TransactionSynchronizationManager.clear()
    }

    @Test
    fun `append stores serialized envelope`() {
        TransactionSynchronizationManager
            .setActualTransactionActive(true)

        val eventId = UUID.randomUUID()
        val aggregateId = UUID.randomUUID()
        val actorUserId = UUID.randomUUID()

        val occurredAt =
            Instant.parse("2026-07-27T14:00:00Z")

        val createdAt =
            Instant.parse("2026-07-27T14:00:01Z")

        val metadata = EventMetadata(
            correlationId = "request-1",
            actorUserId = actorUserId
        )

        lateinit var capturedEnvelope: EventEnvelope<*>

        val serializer = EventEnvelopeSerializer { envelope ->
            capturedEnvelope = envelope
            """{"eventId":"$eventId"}"""
        }

        whenever(
            jdbcOperations.update(
                any<String>(),
                any<SqlParameterSource>()
            )
        ).thenReturn(1)

        val writer = JdbcOutboxEventWriter(
            jdbcOperations = jdbcOperations,
            serializer = serializer,
            metadataProvider = EventMetadataProvider { metadata },
            eventIdGenerator = OutboxEventIdGenerator { eventId },
            clock = OutboxClock { createdAt },
            producer = "account-service",
            requireTransaction = true
        )

        val result = writer.append(
            destination = EventDestination(
                topic = "mnemos.account.events.v1",
                key = aggregateId.toString()
            ),
            event = IntegrationEvent(
                eventType = "account.created",
                eventVersion = 1,
                aggregateType = "ACCOUNT",
                aggregateId = aggregateId,
                occurredAt = occurredAt,
                payload = TestPayload("Knalis")
            )
        )

        assertEquals(eventId, result)

        assertEquals(eventId, capturedEnvelope.eventId)
        assertEquals("account.created", capturedEnvelope.eventType)
        assertEquals("account-service", capturedEnvelope.producer)
        assertEquals(metadata, capturedEnvelope.metadata)

        val sqlCaptor = argumentCaptor<String>()
        val parametersCaptor =
            argumentCaptor<SqlParameterSource>()

        verify(jdbcOperations).update(
            sqlCaptor.capture(),
            parametersCaptor.capture()
        )

        val parameters = parametersCaptor.firstValue

        assertEquals(true, sqlCaptor.firstValue.contains("outbox_events"))
        assertEquals(eventId, parameters.getValue("id"))
        assertEquals(
            "mnemos.account.events.v1",
            parameters.getValue("topic")
        )
        assertEquals(
            aggregateId.toString(),
            parameters.getValue("eventKey")
        )
        assertEquals(
            "account.created",
            parameters.getValue("eventType")
        )
        assertEquals(
            aggregateId,
            parameters.getValue("aggregateId")
        )
        assertEquals(
            actorUserId,
            parameters.getValue("actorUserId")
        )
        assertEquals(
            occurredAt.atOffset(ZoneOffset.UTC),
            parameters.getValue("occurredAt")
        )
        assertEquals(
            createdAt.atOffset(ZoneOffset.UTC),
            parameters.getValue("createdAt")
        )
    }

    @Test
    fun `explicit metadata overrides metadata provider`() {
        TransactionSynchronizationManager
            .setActualTransactionActive(true)

        val eventId = UUID.randomUUID()

        val providerMetadata = EventMetadata(
            correlationId = "provider"
        )

        val explicitMetadata = EventMetadata(
            correlationId = "explicit"
        )

        lateinit var capturedEnvelope: EventEnvelope<*>

        whenever(
            jdbcOperations.update(
                any<String>(),
                any<SqlParameterSource>()
            )
        ).thenReturn(1)

        val writer = JdbcOutboxEventWriter(
            jdbcOperations = jdbcOperations,
            serializer = EventEnvelopeSerializer { envelope ->
                capturedEnvelope = envelope
                "{}"
            },
            metadataProvider = EventMetadataProvider {
                providerMetadata
            },
            eventIdGenerator = OutboxEventIdGenerator {
                eventId
            },
            clock = OutboxClock {
                Instant.parse("2026-07-27T14:00:00Z")
            },
            producer = "account-service",
            requireTransaction = true
        )

        writer.append(
            destination = EventDestination(
                topic = "mnemos.account.events.v1",
                key = UUID.randomUUID().toString()
            ),
            event = IntegrationEvent(
                eventType = "account.created",
                eventVersion = 1,
                aggregateType = "ACCOUNT",
                aggregateId = UUID.randomUUID(),
                occurredAt = Instant.now(),
                payload = TestPayload("Knalis")
            ),
            metadata = explicitMetadata
        )

        assertEquals(
            explicitMetadata,
            capturedEnvelope.metadata
        )
    }

    @Test
    fun `append outside transaction is rejected`() {
        val writer = JdbcOutboxEventWriter(
            jdbcOperations = jdbcOperations,
            serializer = EventEnvelopeSerializer { "{}" },
            metadataProvider = EventMetadataProvider {
                EventMetadata()
            },
            eventIdGenerator = OutboxEventIdGenerator {
                UUID.randomUUID()
            },
            clock = OutboxClock(Instant::now),
            producer = "account-service",
            requireTransaction = true
        )

        assertThrows(
            OutboxTransactionRequiredException::class.java
        ) {
            writer.append(
                destination = EventDestination(
                    topic = "mnemos.account.events.v1",
                    key = UUID.randomUUID().toString()
                ),
                event = IntegrationEvent(
                    eventType = "account.created",
                    eventVersion = 1,
                    aggregateType = "ACCOUNT",
                    aggregateId = UUID.randomUUID(),
                    occurredAt = Instant.now(),
                    payload = TestPayload("Knalis")
                )
            )
        }

        verifyNoInteractions(jdbcOperations)
    }

    private data class TestPayload(
        val nickname: String
    )
}