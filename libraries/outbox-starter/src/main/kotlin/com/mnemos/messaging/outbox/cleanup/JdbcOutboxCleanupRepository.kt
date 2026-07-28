package com.mnemos.messaging.outbox.cleanup

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import java.time.Instant
import java.time.ZoneOffset

class JdbcOutboxCleanupRepository(
    private val jdbcOperations: NamedParameterJdbcOperations
) : OutboxCleanupRepository {

    override fun deleteExpired(
        publishedBefore: Instant,
        deadBefore: Instant,
        batchSize: Int
    ): Int {
        val parameters = MapSqlParameterSource()
            .addValue(
                "publishedBefore",
                publishedBefore.atOffset(ZoneOffset.UTC)
            )
            .addValue(
                "deadBefore",
                deadBefore.atOffset(ZoneOffset.UTC)
            )
            .addValue("batchSize", batchSize)

        return jdbcOperations.queryForObject(
            DELETE_EXPIRED_SQL,
            parameters,
            Long::class.java
        )?.toInt() ?: 0
    }

    private companion object {
        const val DELETE_EXPIRED_SQL = """
            WITH candidates AS (
                SELECT id
                FROM outbox_events
                WHERE (
                    status = 'PUBLISHED'
                    AND published_at < :publishedBefore
                )
                OR (
                    status = 'DEAD'
                    AND dead_at < :deadBefore
                )
                ORDER BY
                    COALESCE(published_at, dead_at) ASC,
                    id ASC
                LIMIT :batchSize
                FOR UPDATE SKIP LOCKED
            ),
            deleted AS (
                DELETE FROM outbox_events event
                USING candidates
                WHERE event.id = candidates.id
                RETURNING event.id
            )
            SELECT COUNT(*)
            FROM deleted
        """
    }
}