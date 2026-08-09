package com.mnemos.messaging.inbox.cleanup

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import java.time.Instant
import java.time.ZoneOffset

class JdbcInboxCleanupRepository(
    private val jdbcOperations: NamedParameterJdbcOperations,
) : InboxCleanupRepository {

    override fun deleteExpired(recordedBefore: Instant, batchSize: Int): Int {
        val parameters = MapSqlParameterSource()
            .addValue("recordedBefore", recordedBefore.atOffset(ZoneOffset.UTC))
            .addValue("batchSize", batchSize)

        return jdbcOperations.queryForObject(
            DELETE_EXPIRED_SQL,
            parameters,
            Long::class.java,
        )?.toInt() ?: 0
    }

    private companion object {
        const val DELETE_EXPIRED_SQL = """
            WITH candidates AS (
                SELECT consumer, event_id
                FROM inbox_events
                WHERE recorded_at < :recordedBefore
                ORDER BY recorded_at ASC, consumer ASC, event_id ASC
                LIMIT :batchSize
                FOR UPDATE SKIP LOCKED
            ),
            deleted AS (
                DELETE FROM inbox_events event
                USING candidates
                WHERE event.consumer = candidates.consumer
                  AND event.event_id = candidates.event_id
                RETURNING event.event_id
            )
            SELECT COUNT(*)
            FROM deleted
        """
    }
}