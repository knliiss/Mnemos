package com.mnemos.messaging.outbox.writer

import com.mnemos.messaging.core.event.EventEnvelope
import com.mnemos.messaging.core.event.EventMetadata
import com.mnemos.messaging.core.event.IntegrationEvent
import com.mnemos.messaging.core.publisher.IntegrationEventRoute
import com.mnemos.messaging.core.transport.EventDestination
import com.mnemos.messaging.outbox.metadata.EventMetadataProvider
import com.mnemos.messaging.outbox.serialization.EventEnvelopeSerializer
import com.mnemos.messaging.outbox.support.OutboxClock
import com.mnemos.messaging.outbox.support.OutboxEventIdGenerator
import org.slf4j.LoggerFactory
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.sql.Types
import java.time.ZoneOffset
import java.util.UUID

class JdbcOutboxEventWriter(
    private val jdbcOperations: NamedParameterJdbcOperations,
    private val serializer: EventEnvelopeSerializer,
    private val eventIdGenerator: OutboxEventIdGenerator,
    private val clock: OutboxClock,
    private val producer: String,
    private val requireTransaction: Boolean
) : OutboxEventWriter {

    override fun <T : Any> append(
        route: IntegrationEventRoute,
        event: IntegrationEvent<T>,
        metadata: EventMetadata,
    ): UUID {
        logger.debug(
            "Appending outbox row: type={}, aggregateId={}, topic={}, version={}, aggregateType={}, transactionActive={}",
            event.type,
            event.aggregateId,
            route.topic,
            route.version,
            route.aggregateType,
            TransactionSynchronizationManager.isActualTransactionActive(),
        )
        requireActiveTransaction()

        val eventId = eventIdGenerator.generate()
        val createdAt = clock.now()

        val envelope =
            EventEnvelope.from(
                eventId = eventId,
                producer = producer,
                metadata = metadata,
                event = event,
                route = route,
            )

        val parameters =
            MapSqlParameterSource()
                .addValue(
                    "id",
                    eventId,
                    Types.OTHER,
                )
                .addValue(
                    "topic",
                    route.topic,
                    Types.VARCHAR,
                )
                .addValue(
                    "eventKey",
                    event.aggregateId.toString(),
                    Types.VARCHAR,
                )
                .addValue(
                    "eventType",
                    event.type,
                    Types.VARCHAR,
                )
                .addValue(
                    "eventVersion",
                    route.version,
                    Types.INTEGER,
                )
                .addValue(
                    "producer",
                    producer,
                    Types.VARCHAR,
                )
                .addValue(
                    "aggregateType",
                    route.aggregateType,
                    Types.VARCHAR,
                )
                .addValue(
                    "aggregateId",
                    event.aggregateId,
                    Types.OTHER,
                )
                .addValue(
                    "correlationId",
                    metadata.correlationId,
                    Types.VARCHAR,
                )
                .addValue(
                    "causationId",
                    metadata.causationId,
                    Types.OTHER,
                )
                .addValue(
                    "actorUserId",
                    metadata.actorUserId,
                    Types.OTHER,
                )
                .addValue(
                    "envelope",
                    serializer.serialize(envelope),
                    Types.VARCHAR,
                )
                .addValue(
                    "occurredAt",
                    event.occurredAt.atOffset(ZoneOffset.UTC),
                    Types.TIMESTAMP_WITH_TIMEZONE,
                )
                .addValue(
                    "createdAt",
                    createdAt.atOffset(ZoneOffset.UTC),
                    Types.TIMESTAMP_WITH_TIMEZONE,
                )

        val insertedRows = jdbcOperations.update(
            INSERT_SQL,
            parameters
        )

        logger.debug(
            "Outbox INSERT completed: messageId={}, type={}, aggregateId={}, insertedRows={}",
            eventId,
            event.type,
            event.aggregateId,
            insertedRows,
        )

        check(insertedRows == 1) {
            "Expected one outbox row to be inserted, but inserted $insertedRows"
        }

        return eventId
    }

    private fun requireActiveTransaction() {
        if (!requireTransaction) {
            logger.debug("Outbox transaction requirement is disabled")
            return
        }

        val transactionActive =
            TransactionSynchronizationManager.isActualTransactionActive()

        if (!transactionActive) {
            logger.error(
                "Attempted to append outbox event without an active Spring transaction",
            )

            throw OutboxTransactionRequiredException()
        }
    }

    private companion object {
        val logger = LoggerFactory.getLogger(JdbcOutboxEventWriter::class.java)
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
                failure_count,
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