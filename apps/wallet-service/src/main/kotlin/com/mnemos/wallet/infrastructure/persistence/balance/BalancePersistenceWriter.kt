package com.mnemos.wallet.infrastructure.persistence.balance

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
                .addValue("accountId", balance.id.accountId, Types.OTHER)
                .addValue("currency", balance.id.currency.name, Types.VARCHAR)
                .addValue("amount", balance.amount, Types.BIGINT)
                .addValue("lastTransactionId", balance.lastTransactionId, Types.OTHER)
                .addValue(
                    "createdAt",
                    balance.createdAt.atOffset(ZoneOffset.UTC),
                    Types.TIMESTAMP_WITH_TIMEZONE,
                ).addValue(
                    "updatedAt",
                    balance.updatedAt.atOffset(ZoneOffset.UTC),
                    Types.TIMESTAMP_WITH_TIMEZONE,
                )

        return jdbcOperations.update(INSERT_IF_ABSENT_SQL, parameters) == 1
    }

    private companion object {
        const val INSERT_IF_ABSENT_SQL = """
            INSERT INTO balances (
                account_id,
                currency,
                amount,
                last_transaction_id,
                created_at,
                updated_at
            ) VALUES (
                :accountId,
                :currency,
                :amount,
                :lastTransactionId,
                :createdAt,
                :updatedAt
            )
            ON CONFLICT (account_id, currency) DO NOTHING
        """
    }
}
