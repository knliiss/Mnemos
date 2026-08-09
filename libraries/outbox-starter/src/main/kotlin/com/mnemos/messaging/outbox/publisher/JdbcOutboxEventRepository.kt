package com.mnemos.messaging.outbox.publisher

import org.springframework.jdbc.core.RowMapper
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.transaction.support.TransactionOperations
import java.sql.ResultSet
import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset
import java.util.UUID

class JdbcOutboxEventRepository(
    private val jdbcOperations: NamedParameterJdbcOperations,
    private val transactionOperations: TransactionOperations,
) : OutboxEventRepository {

    override fun claimNextPending(
        instanceId: String,
        now: Instant,
        lockedUntil: Instant,
    ): OutboxEventRecord? =
        transactionOperations.execute {
            val parameters =
                MapSqlParameterSource()
                    .addValue("instanceId", instanceId)
                    .addValue(
                        "now",
                        now.atOffset(ZoneOffset.UTC),
                    )
                    .addValue(
                        "lockedUntil",
                        lockedUntil.atOffset(ZoneOffset.UTC),
                    )

            jdbcOperations.query(
                CLAIM_NEXT_PENDING_SQL,
                parameters,
                ROW_MAPPER,
            ).firstOrNull()
        }

    override fun markPublished(
        eventId: UUID,
        instanceId: String,
        publishedAt: Instant,
    ): Boolean =
        transactionOperations.execute {
            val parameters =
                MapSqlParameterSource()
                    .addValue("eventId", eventId)
                    .addValue("instanceId", instanceId)
                    .addValue(
                        "publishedAt",
                        publishedAt.atOffset(ZoneOffset.UTC),
                    )

            jdbcOperations.update(
                MARK_PUBLISHED_SQL,
                parameters,
            ) == 1
        } ?: false

    override fun markFailed(
        eventId: UUID,
        instanceId: String,
        previousFailureCount: Int,
        failureCount: Int,
        nextAttemptAt: Instant,
        error: String,
    ): Boolean =
        transactionOperations.execute {
            val parameters =
                MapSqlParameterSource()
                    .addValue("eventId", eventId)
                    .addValue("instanceId", instanceId)
                    .addValue(
                        "previousFailureCount",
                        previousFailureCount,
                    )
                    .addValue(
                        "failureCount",
                        failureCount,
                    )
                    .addValue(
                        "nextAttemptAt",
                        nextAttemptAt.atOffset(ZoneOffset.UTC),
                    )
                    .addValue("error", error)

            jdbcOperations.update(
                MARK_FAILED_SQL,
                parameters,
            ) == 1
        } ?: false

    override fun markDead(
        eventId: UUID,
        instanceId: String,
        previousFailureCount: Int,
        failureCount: Int,
        deadAt: Instant,
        error: String,
    ): Boolean =
        transactionOperations.execute {
            val parameters =
                MapSqlParameterSource()
                    .addValue("eventId", eventId)
                    .addValue("instanceId", instanceId)
                    .addValue(
                        "previousFailureCount",
                        previousFailureCount,
                    )
                    .addValue(
                        "failureCount",
                        failureCount,
                    )
                    .addValue(
                        "deadAt",
                        deadAt.atOffset(ZoneOffset.UTC),
                    )
                    .addValue("error", error)

            jdbcOperations.update(
                MARK_DEAD_SQL,
                parameters,
            ) == 1
        } ?: false

    private companion object {

        val ROW_MAPPER =
            RowMapper { result: ResultSet, _: Int ->
                OutboxEventRecord(
                    id =
                        result.getObject(
                            "id",
                            UUID::class.java,
                        ),
                    topic = result.getString("topic"),
                    eventKey = result.getString("event_key"),
                    eventType = result.getString("event_type"),
                    eventVersion = result.getInt("event_version"),
                    producer = result.getString("producer"),
                    aggregateType =
                        result.getString("aggregate_type"),
                    aggregateId =
                        result.getObject(
                            "aggregate_id",
                            UUID::class.java,
                        ),
                    correlationId =
                        result.getString("correlation_id"),
                    causationId =
                        result.getObject(
                            "causation_id",
                            UUID::class.java,
                        ),
                    actorUserId =
                        result.getObject(
                            "actor_user_id",
                            UUID::class.java,
                        ),
                    envelope = result.getString("envelope"),
                    occurredAt =
                        result
                            .getObject(
                                "occurred_at",
                                OffsetDateTime::class.java,
                            )
                            .toInstant(),
                    failureCount = result.getInt("failure_count"),
                )
            }

        const val CLAIM_NEXT_PENDING_SQL = """
            WITH candidate AS (
                SELECT current_event.id
                FROM outbox_events current_event
                WHERE current_event.status = 'PENDING'
                  AND current_event.next_attempt_at <= :now
                  AND (
                      current_event.locked_until IS NULL
                      OR current_event.locked_until < :now
                  )
                  AND NOT EXISTS (
                      SELECT 1
                      FROM outbox_events previous_event
                      WHERE previous_event.status = 'PENDING'
                        AND previous_event.aggregate_type =
                            current_event.aggregate_type
                        AND previous_event.aggregate_id =
                            current_event.aggregate_id
                        AND previous_event.sequence_no <
                            current_event.sequence_no
                  )
                ORDER BY
                    current_event.next_attempt_at ASC,
                    current_event.sequence_no ASC
                LIMIT 1
                FOR UPDATE OF current_event SKIP LOCKED
            )
            UPDATE outbox_events event
            SET locked_by = :instanceId,
                locked_until = :lockedUntil
            FROM candidate
            WHERE event.id = candidate.id
            RETURNING
                event.id,
                event.topic,
                event.event_key,
                event.event_type,
                event.event_version,
                event.producer,
                event.aggregate_type,
                event.aggregate_id,
                event.correlation_id,
                event.causation_id,
                event.actor_user_id,
                event.envelope::text AS envelope,
                event.occurred_at,
                event.failure_count
"""

        const val MARK_PUBLISHED_SQL = """
            UPDATE outbox_events
            SET status = 'PUBLISHED',
                published_at = :publishedAt,
                locked_by = NULL,
                locked_until = NULL,
                last_error = NULL
            WHERE id = :eventId
              AND status = 'PENDING'
              AND locked_by = :instanceId
        """

        const val MARK_FAILED_SQL = """
            UPDATE outbox_events
            SET failure_count = :failureCount,
                next_attempt_at = :nextAttemptAt,
                locked_by = NULL,
                locked_until = NULL,
                last_error = :error
            WHERE id = :eventId
              AND status = 'PENDING'
              AND locked_by = :instanceId
              AND failure_count = :previousFailureCount
"""

        const val MARK_DEAD_SQL = """
            UPDATE outbox_events
            SET status = 'DEAD',
                failure_count = :failureCount,
                dead_at = :deadAt,
                locked_by = NULL,
                locked_until = NULL,
                last_error = :error
            WHERE id = :eventId
              AND status = 'PENDING'
              AND locked_by = :instanceId
              AND failure_count = :previousFailureCount
        """
    }
}