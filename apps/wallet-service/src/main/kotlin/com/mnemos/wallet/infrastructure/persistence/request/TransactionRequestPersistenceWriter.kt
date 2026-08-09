package com.mnemos.wallet.infrastructure.persistence.request

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.stereotype.Component
import java.sql.Types
import java.time.ZoneOffset

@Component
class TransactionRequestPersistenceWriter(
    private val jdbcOperations: NamedParameterJdbcOperations,
) {
    fun insertIfAbsent(request: TransactionRequestEntity): Boolean {
        val parameters =
            MapSqlParameterSource()
                .addValue("requestId", request.requestId, Types.OTHER)
                .addValue("accountId", request.accountId, Types.OTHER)
                .addValue("currency", request.currency.name, Types.VARCHAR)
                .addValue("type", request.type.name, Types.VARCHAR)
                .addValue("source", request.source.name, Types.VARCHAR)
                .addValue("sourceReferenceId", request.sourceReferenceId, Types.OTHER)
                .addValue("amount", request.amount, Types.BIGINT)
                .addValue("status", request.status.name, Types.VARCHAR)
                .addValue("transactionId", request.transactionId, Types.OTHER)
                .addValue("failureReason", request.failureReason?.name, Types.VARCHAR)
                .addValue(
                    "createdAt",
                    request.createdAt.atOffset(ZoneOffset.UTC),
                    Types.TIMESTAMP_WITH_TIMEZONE,
                ).addValue(
                    "completedAt",
                    request.completedAt?.atOffset(ZoneOffset.UTC),
                    Types.TIMESTAMP_WITH_TIMEZONE,
                )

        return jdbcOperations.update(INSERT_IF_ABSENT_SQL, parameters) == 1
    }

    private companion object {
        const val INSERT_IF_ABSENT_SQL = """
            INSERT INTO transaction_requests (
                request_id,
                account_id,
                currency,
                type,
                source,
                source_reference_id,
                amount,
                status,
                transaction_id,
                failure_reason,
                created_at,
                completed_at
            ) VALUES (
                :requestId,
                :accountId,
                :currency,
                :type,
                :source,
                :sourceReferenceId,
                :amount,
                :status,
                :transactionId,
                :failureReason,
                :createdAt,
                :completedAt
            )
            ON CONFLICT (request_id) DO NOTHING
        """
    }
}
