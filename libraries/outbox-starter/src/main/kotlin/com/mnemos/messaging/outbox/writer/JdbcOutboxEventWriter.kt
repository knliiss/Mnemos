package com.mnemos.messaging.outbox.writer

import com.mnemos.messaging.core.event.EventEnvelope
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.transport.EventDestination
import com.mnemos.messaging.outbox.metadata.EventMetadataProvider
import com.mnemos.messaging.outbox.serialization.EventEnvelopeSerializer
import com.mnemos.messaging.outbox.support.OutboxClock
import com.mnemos.messaging.outbox.support.OutboxEventIdGenerator
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.sql.Types
import java.time.ZoneOffset
import java.util.UUID

class JdbcOutboxEventWriter(
    private val jdbcOperations: NamedParameterJdbcOperations,
    private val serializer: EventEnvelopeSerializer,
    private val metadataProvider: EventMetadataProvider,
    private val eventIdGenerator: OutboxEventIdGenerator,
    private val clock: OutboxClock,
    private val producer: String,
    private val requireTransaction: Boolean
) : OutboxEventWriter {

    override fun <T : Any> append(
        destination: EventDestination,
        event: IntegrationEvent<T>,
        metadata: EventMetadata?
    ): UUID {
        requireActiveTransaction()

        val eventId = eventIdGenerator.generate()
        val createdAt = clock.now()
        val resolvedMetadata = metadata ?: metadataProvider.current()

        val envelope = EventEnvelope.from(
            eventId = eventId,
            producer = producer,
            metadata = resolvedMetadata,
            event = event
        )

        val parameters = MapSqlParameterSource()
            .addValue("id", eventId, Types.OTHER)
            .addValue("topic", destination.topic, Types.VARCHAR)
            .addValue("eventKey", destination.key, Types.VARCHAR)
            .addValue("eventType", event.eventType, Types.VARCHAR)
            .addValue("eventVersion", event.eventVersion, Types.INTEGER)
            .addValue("producer", producer, Types.VARCHAR)
            .addValue("aggregateType", event.aggregateType, Types.VARCHAR)
            .addValue("aggregateId", event.aggregateId, Types.OTHER)
            .addValue(
                "correlationId",
                resolvedMetadata.correlationId,
                Types.VARCHAR
            )
            .addValue(
                "causationId",
                resolvedMetadata.causationId,
                Types.OTHER
            )
            .addValue(
                "actorUserId",
                resolvedMetadata.actorUserId,
                Types.OTHER
            )
            .addValue(
                "envelope",
                serializer.serialize(envelope),
                Types.VARCHAR
            )
            .addValue(
                "occurredAt",
                event.occurredAt.atOffset(ZoneOffset.UTC),
                Types.TIMESTAMP_WITH_TIMEZONE
            )
            .addValue(
                "createdAt",
                createdAt.atOffset(ZoneOffset.UTC),
                Types.TIMESTAMP_WITH_TIMEZONE
            )

        val insertedRows = jdbcOperations.update(
            INSERT_SQL,
            parameters
        )

        check(insertedRows == 1) {
            "Expected one outbox row to be inserted, but inserted $insertedRows"
        }

        return eventId
    }

    private fun requireActiveTransaction() {
        if (!requireTransaction) {
            return
        }

        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw OutboxTransactionRequiredException()
        }
    }

    private companion object {
        const val INSERT_SQL = """
            INSERT INTO outbox_events (
                id,
                topic,
                event_key,
                event_type,
                event_version,
                producer,
                aggregate_type,
                aggregate_id,
                correlation_id,
                causation_id,
                actor_user_id,
                envelope,
                occurred_at,
                created_at,
                attempts,
                next_attempt_at
            ) VALUES (
                :id,
                :topic,
                :eventKey,
                :eventType,
                :eventVersion,
                :producer,
                :aggregateType,
                :aggregateId,
                :correlationId,
                :causationId,
                :actorUserId,
                CAST(:envelope AS jsonb),
                :occurredAt,
                :createdAt,
                0,
                :createdAt
            )
        """
    }
}