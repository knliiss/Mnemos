package com.mnemos.messaging.outbox.writer

import com.mnemos.messaging.core.event.EventEnvelope
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventRoute
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

        lateinit var capturedEnvelope:
                EventEnvelope<*>

        val serializer =
            EventEnvelopeSerializer { envelope ->
                capturedEnvelope = envelope
                """{"eventId":"$eventId"}"""
            }

        whenever(
            jdbcOperations.update(
                any<String>(),
                any<SqlParameterSource>(),
            ),
        ).thenReturn(1)

        val writer =
            JdbcOutboxEventWriter(
                jdbcOperations = jdbcOperations,
                serializer = serializer,
                eventIdGenerator =
                    OutboxEventIdGenerator {
                        eventId
                    },
                clock =
                    OutboxClock {
                        createdAt
                    },
                producer = "account-service",
                requireTransaction = true,
            )

        val result =
            writer.append(
                route = route,
                event = event,
                metadata = metadata,
            )

        assertEquals(
            eventId,
            result,
        )

        assertEquals(
            eventId,
            capturedEnvelope.eventId,
        )
        assertEquals(
            "account.created",
            capturedEnvelope.eventType,
        )
        assertEquals(
            1,
            capturedEnvelope.eventVersion,
        )
        assertEquals(
            "account-service",
            capturedEnvelope.producer,
        )
        assertEquals(
            "ACCOUNT",
            capturedEnvelope.aggregateType,
        )
        assertEquals(
            aggregateId,
            capturedEnvelope.aggregateId,
        )
        assertEquals(
            metadata,
            capturedEnvelope.metadata,
        )

        val sqlCaptor =
            argumentCaptor<String>()

        val parametersCaptor =
            argumentCaptor<SqlParameterSource>()

        verify(jdbcOperations)
            .update(
                sqlCaptor.capture(),
                parametersCaptor.capture(),
            )

        val parameters =
            parametersCaptor.firstValue

        assertEquals(
            true,
            sqlCaptor.firstValue.contains(
                "outbox_events",
            ),
        )

        assertEquals(
            eventId,
            parameters.getValue("id"),
        )

        assertEquals(
            "mnemos.account.events.v1",
            parameters.getValue("topic"),
        )

        assertEquals(
            aggregateId.toString(),
            parameters.getValue("eventKey"),
        )

        assertEquals(
            "account.created",
            parameters.getValue("eventType"),
        )

        assertEquals(
            1,
            parameters.getValue("eventVersion"),
        )

        assertEquals(
            "ACCOUNT",
            parameters.getValue("aggregateType"),
        )

        assertEquals(
            aggregateId,
            parameters.getValue("aggregateId"),
        )

        assertEquals(
            "request-1",
            parameters.getValue("correlationId"),
        )

        assertEquals(
            actorUserId,
            parameters.getValue("actorUserId"),
        )

        assertEquals(
            occurredAt.atOffset(
                ZoneOffset.UTC,
            ),
            parameters.getValue("occurredAt"),
        )

        assertEquals(
            createdAt.atOffset(
                ZoneOffset.UTC,
            ),
            parameters.getValue("createdAt"),
        )
    }

    @Test
    fun `append stores supplied metadata unchanged`() {
        TransactionSynchronizationManager
            .setActualTransactionActive(true)

        val eventId =
            UUID.randomUUID()

        val causationId =
            UUID.randomUUID()

        val metadata =
            EventMetadata(
                correlationId = "request-1",
                causationId = causationId,
            )

        lateinit var capturedEnvelope:
                EventEnvelope<*>

        whenever(
            jdbcOperations.update(
                any<String>(),
                any<SqlParameterSource>(),
            ),
        ).thenReturn(1)

        val writer =
            JdbcOutboxEventWriter(
                jdbcOperations = jdbcOperations,
                serializer =
                    EventEnvelopeSerializer { envelope ->
                        capturedEnvelope = envelope
                        "{}"
                    },
                eventIdGenerator =
                    OutboxEventIdGenerator {
                        eventId
                    },
                clock =
                    OutboxClock {
                        Instant.parse(
                            "2026-07-27T14:00:00Z",
                        )
                    },
                producer = "account-service",
                requireTransaction = true,
            )

        writer.append(
            route = route(),
            event = event(),
            metadata = metadata,
        )

        assertEquals(
            metadata,
            capturedEnvelope.metadata,
        )
    }

    @Test
    fun `append outside transaction is rejected`() {
        val writer =
            JdbcOutboxEventWriter(
                jdbcOperations = jdbcOperations,
                serializer =
                    EventEnvelopeSerializer {
                        "{}"
                    },
                eventIdGenerator =
                    OutboxEventIdGenerator {
                        UUID.randomUUID()
                    },
                clock =
                    OutboxClock(
                        Instant::now,
                    ),
                producer = "account-service",
                requireTransaction = true,
            )

        assertThrows(
            OutboxTransactionRequiredException::class.java,
        ) {
            writer.append(
                route = route(),
                event = event(),
                metadata = EventMetadata(),
            )
        }

        verifyNoInteractions(
            jdbcOperations,
        )
    }

    private fun route(): IntegrationEventRoute =
        IntegrationEventRoute(
            topic = "mnemos.account.events.v1",
            version = 1,
            aggregateType = "ACCOUNT",
        )

    private fun event():
            IntegrationEvent<TestPayload> =
        IntegrationEvent(
            type = "account.created",
            aggregateId = UUID.randomUUID(),
            occurredAt = Instant.now(),
            payload = TestPayload("Knalis"),
        )

    private data class TestPayload(
        val nickname: String,
    )
}