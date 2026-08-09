package com.mnemos.wallet.infrastructure.persistence.balance

import com.mnemos.wallet.contracts.wallet.shared.Currency
import jakarta.persistence.LockModeType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Lock
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import java.time.Instant
import java.util.UUID

interface BalanceJpaRepository : JpaRepository<BalanceEntity, BalanceEntityId> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query(
        """
        SELECT balance
        FROM BalanceEntity balance
        WHERE balance.id.accountId = :accountId
          AND balance.id.currency = :currency
        """,
    )
    fun findByAccountIdAndCurrencyForUpdate(
        @Param("accountId") accountId: UUID,
        @Param("currency") currency: Currency,
    ): BalanceEntity?

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        UPDATE BalanceEntity balance
        SET balance.amount = :amount,
            balance.lastTransactionId = :lastTransactionId,
            balance.updatedAt = :updatedAt
        WHERE balance.id.accountId = :accountId
          AND balance.id.currency = :currency
        """,
    )
    fun updateBalance(
        @Param("accountId") accountId: UUID,
        @Param("currency") currency: Currency,
        @Param("amount") amount: Long,
        @Param("lastTransactionId") lastTransactionId: UUID?,
        @Param("updatedAt") updatedAt: Instant,
    ): Int

    @Query(
        """
        SELECT BalanceEntity b
        FROM BalanceEntity balance
        WHERE balance.id.accountId = :accountId
        ORDER BY balance.id.currency DESC
    """,
    )
    fun findAllByAccountId(id: UUID): List<BalanceEntity>
}
