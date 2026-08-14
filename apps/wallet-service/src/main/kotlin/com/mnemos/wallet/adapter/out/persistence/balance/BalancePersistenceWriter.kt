package com.mnemos.wallet.adapter.out.persistence.balance

import org.springframework.jdbc.core.namedparam.MapSqlParameterSource
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations
import org.springframework.stereotype.Component
import java.sql.Types
import java.time.ZoneOffset

@Component
class BalancePersistenceWriter(
    private val jdbcOperations: NamedParameterJdbcOperations,
) {
    fun insertIfAbsent(balance: BalanceEntity): Boolean {
        val parameters =
            MapSqlParameterSource()
                .addValue("ownerType", balance.id.ownerType.name, Types.VARCHAR)
                .addValue("ownerId", balance.id.ownerId, Types.OTHER)
                .addValue("currency", balance.id.currency.name, Types.VARCHAR)
                .addValue("amount", balance.amount, Types.BIGINT)
                .addValue("lastTransactionId", balance.lastTransactionId, Types.OTHER)
                .addValue("createdAt", balance.createdAt.atOffset(ZoneOffset.UTC), Types.TIMESTAMP_WITH_TIMEZONE)
                .addValue("updatedAt", balance.updatedAt.atOffset(ZoneOffset.UTC), Types.TIMESTAMP_WITH_TIMEZONE)

        return jdbcOperations.update(INSERT_IF_ABSENT_SQL, parameters) == 1
    }

    private companion object {
        const val INSERT_IF_ABSENT_SQL = """
            INSERT INTO balances (
                owner_type,
                owner_id,
                currency,
                amount,
                last_transaction_id,
                created_at,
                updated_at
            ) VALUES (
                :ownerType,
                :ownerId,
                :currency,
                :amount,
                :lastTransactionId,
                :createdAt,
                :updatedAt
            )
            ON CONFLICT (owner_type, owner_id, currency) DO NOTHING
        """
    }
}
