package com.mnemos.wallet.adapter.out.persistence.balance

import com.mnemos.wallet.contracts.wallet.shared.Currency
import com.mnemos.wallet.contracts.wallet.shared.WalletOwnerType
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
        WHERE balance.id.ownerType = :ownerType
          AND balance.id.ownerId = :ownerId
          AND balance.id.currency = :currency
        """,
    )
    fun findForUpdate(
        @Param("ownerType") ownerType: WalletOwnerType,
        @Param("ownerId") ownerId: UUID,
        @Param("currency") currency: Currency,
    ): BalanceEntity?

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(
        """
        UPDATE BalanceEntity balance
        SET balance.amount = :amount,
            balance.lastTransactionId = :lastTransactionId,
            balance.updatedAt = :updatedAt
        WHERE balance.id.ownerType = :ownerType
          AND balance.id.ownerId = :ownerId
          AND balance.id.currency = :currency
        """,
    )
    fun updateBalance(
        @Param("ownerType") ownerType: WalletOwnerType,
        @Param("ownerId") ownerId: UUID,
        @Param("currency") currency: Currency,
        @Param("amount") amount: Long,
        @Param("lastTransactionId") lastTransactionId: UUID?,
        @Param("updatedAt") updatedAt: Instant,
    ): Int

    @Query(
        """
        SELECT balance
        FROM BalanceEntity balance
        WHERE balance.id.ownerType = :ownerType
          AND balance.id.ownerId = :ownerId
        ORDER BY balance.id.currency ASC
        """,
    )
    fun findAllByOwner(
        @Param("ownerType") ownerType: WalletOwnerType,
        @Param("ownerId") ownerId: UUID,
    ): List<BalanceEntity>
}
