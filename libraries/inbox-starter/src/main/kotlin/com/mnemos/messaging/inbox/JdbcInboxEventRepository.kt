package com.mnemos.messaging.inbox

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import java.sql.Types
import java.time.Instant
import java.time.ZoneOffset
import java.util.UUID

@Suppress("SqlResolve")
class JdbcInboxEventRepository(
    private val jdbcOperations: NamedParameterJdbcOperations,
) : InboxEventRepository {

    override fun insertIfAbsent(
        consumer: String,
        eventId: UUID,
        recordedAt: Instant,
    ): Boolean {
        val parameters =
            MapSqlParameterSource()
                .addValue(
                    "consumer",
                    consumer,
                    Types.VARCHAR,
                )
                .addValue(
                    "eventId",
                    eventId,
                    Types.OTHER,
                )
                .addValue(
                    "recordedAt",
                    recordedAt.atOffset(
                        ZoneOffset.UTC,
                    ),
                    Types.TIMESTAMP_WITH_TIMEZONE,
                )

        return jdbcOperations.update(
            INSERT_IF_ABSENT_SQL,
            parameters,
        ) == 1
    }

    private companion object {

        const val INSERT_IF_ABSENT_SQL = """
            INSERT INTO inbox_events (
                consumer,
                event_id,
                recorded_at
            )
            VALUES (
                :consumer,
                :eventId,
                :recordedAt
            )
            ON CONFLICT (
                consumer,
                event_id
            )
            DO NOTHING
        """
    }
}